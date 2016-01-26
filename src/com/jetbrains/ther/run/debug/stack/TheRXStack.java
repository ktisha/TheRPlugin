package com.jetbrains.ther.run.debug.stack;

import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.ther.debugger.frame.TheRStackFrame;
import com.jetbrains.ther.run.debug.resolve.TheRResolvingSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;

public class TheRXStack {

  @NotNull
  private final List<TheRStackFrame> myOriginalStack;

  @NotNull
  private final TheRResolvingSession mySession;

  @NotNull
  private final ExecutorService myExecutor;

  @Nullable
  private List<TheRXStackFrame> myStack;

  @Nullable
  private XSuspendContext mySuspendContext;

  public TheRXStack(@NotNull final List<TheRStackFrame> stack,
                    @NotNull final TheRResolvingSession session,
                    @NotNull final ExecutorService executor) {
    myOriginalStack = stack;
    mySession = session;
    myExecutor = executor;
    myStack = null;
    mySuspendContext = null;
  }

  public void update() {
    // `myOriginalStack` ends with newest frame and `myStack` ends with eldest frame

    myStack = calculateStack(myStack, myOriginalStack);
    mySuspendContext = new TheRXSuspendContext(myStack);
  }

  @NotNull
  public XSuspendContext getSuspendContext() {
    if (mySuspendContext == null || myStack == null) {
      throw new IllegalStateException("GetSuspendContext could be called only after update");
    }

    return mySuspendContext;
  }

  @NotNull
  private List<TheRXStackFrame> calculateStack(@Nullable final List<TheRXStackFrame> previousStack,
                                               @NotNull final List<TheRStackFrame> currentStack) {
    if (previousStack == null) {
      return calculateStackFully(currentStack);
    }

    final int prevSize = previousStack.size();
    final int currentSize = currentStack.size();

    if (prevSize == currentSize) {
      return calculateStackOnTheSameDepth(previousStack, currentStack);
    }
    else if (currentSize > prevSize) {
      return calculateStackOnTheMoreDepth(previousStack, currentStack);
    }
    else {
      return calculateStackOnTheLessDepth(previousStack, currentStack);
    }
  }

  @NotNull
  private List<TheRXStackFrame> calculateStackFully(@NotNull final List<TheRStackFrame> currentStack) {
    final TheRXStackFrame[] result = new TheRXStackFrame[currentStack.size()];

    int index = 0;
    for (final TheRStackFrame frame : currentStack) {
      result[result.length - 1 - index] =
        new TheRXStackFrame(
          frame,
          mySession.resolveNext(frame.getLocation()),
          myExecutor
        );

      index++;
    }

    return Arrays.asList(result);
  }

  @NotNull
  private List<TheRXStackFrame> calculateStackOnTheSameDepth(@NotNull final List<TheRXStackFrame> previousStack,
                                                             @NotNull final List<TheRStackFrame> currentStack) {
    final List<TheRXStackFrame> result = new ArrayList<TheRXStackFrame>(previousStack);
    final TheRStackFrame lastFrame = currentStack.get(currentStack.size() - 1);

    result.set(
      0,
      new TheRXStackFrame(
        lastFrame,
        mySession.resolveCurrent(
          lastFrame.getLocation().getLine()
        ),
        myExecutor
      )
    );

    return result;
  }

  @NotNull
  private List<TheRXStackFrame> calculateStackOnTheMoreDepth(@NotNull final List<TheRXStackFrame> previousStack,
                                                             @NotNull final List<TheRStackFrame> currentStack) {
    final TheRXStackFrame[] result = new TheRXStackFrame[currentStack.size()];
    final int offset = currentStack.size() - previousStack.size();

    int index = 0;
    for (final TheRXStackFrame previousFrame : previousStack) {
      result[offset + index] = previousFrame;

      index++;
    }

    final ListIterator<TheRStackFrame> currentStackIterator = currentStack.listIterator(previousStack.size());

    for (int i = 0; i < offset; i++) {
      final TheRStackFrame frame = currentStackIterator.next();

      result[offset - 1 - i] =
        new TheRXStackFrame(
          frame,
          mySession.resolveNext(frame.getLocation()),
          myExecutor
        );
    }

    return Arrays.asList(result);
  }

  @NotNull
  private List<TheRXStackFrame> calculateStackOnTheLessDepth(@NotNull final List<TheRXStackFrame> previousStack,
                                                             @NotNull final List<TheRStackFrame> currentStack) {
    final TheRXStackFrame[] result = new TheRXStackFrame[currentStack.size()];
    final int offset = previousStack.size() - currentStack.size();

    final ListIterator<TheRXStackFrame> previousStackIterator = previousStack.listIterator(offset);

    for (int i = 0; i < currentStack.size(); i++) {
      result[i] = previousStackIterator.next();
    }

    final TheRStackFrame lastFrame = currentStack.get(currentStack.size() - 1);

    mySession.dropLast(offset);

    result[0] =
      new TheRXStackFrame(
        lastFrame,
        mySession.resolveCurrent(
          lastFrame.getLocation().getLine()
        ),
        myExecutor
      );

    return Arrays.asList(result);
  }
}
