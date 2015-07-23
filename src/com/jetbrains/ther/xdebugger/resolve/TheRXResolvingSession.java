package com.jetbrains.ther.xdebugger.resolve;

import com.intellij.xdebugger.XSourcePosition;
import com.jetbrains.ther.debugger.data.TheRLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

// TODO [xdbg][test]
public class TheRXResolvingSession {

  @NotNull
  private final TheRXResolver myResolver;

  @NotNull
  private final List<TheRXResolvingSessionEntry> myEntries;

  public TheRXResolvingSession(@NotNull final TheRXResolver resolver) {
    myResolver = resolver;
    myEntries = new ArrayList<TheRXResolvingSessionEntry>();
  }

  @Nullable
  public XSourcePosition resolveNext(@NotNull final TheRLocation nextLocation) {
    appendEntry(nextLocation);

    return resolveLastEntry();
  }

  @Nullable
  public XSourcePosition resolveLast(final int line) {
    final int lastIndex = myEntries.size() - 1;

    myEntries.set(
      lastIndex,
      new TheRXResolvingSessionEntry(
        myEntries.get(lastIndex).myDescriptor,
        line
      )
    );

    return resolveLastEntry();
  }

  public void dropLast(final int number) {
    final ListIterator<TheRXResolvingSessionEntry> iterator = myEntries.listIterator(myEntries.size());
    iterator.previous();

    for (int i = 0; i < number; i++) {
      iterator.remove();
      iterator.previous();
    }
  }

  private void appendEntry(@NotNull final TheRLocation nextLocation) {
    final int line = nextLocation.getLine();

    if (myEntries.isEmpty()) {
      myEntries.add(
        new TheRXResolvingSessionEntry(myResolver.getRoot(), line)
      );
    }
    else {
      myEntries.add(
        new TheRXResolvingSessionEntry(
          resolveNextDescriptor(myEntries.get(myEntries.size() - 1), nextLocation),
          line
        )
      );
    }
  }

  @Nullable
  private XSourcePosition resolveLastEntry() {
    final TheRXResolvingSessionEntry lastEntry = myEntries.get(myEntries.size() - 1);

    if (lastEntry.myDescriptor == null) {
      return null;
    }

    return myResolver.resolve(lastEntry.myDescriptor, lastEntry.myLine);
  }

  @Nullable
  private TheRXFunctionDescriptor resolveNextDescriptor(@NotNull final TheRXResolvingSessionEntry lastEntry,
                                                        @NotNull final TheRLocation nextLocation) {
    if (lastEntry.myDescriptor == null) {
      return null;
    }
    else {
      return TheRXFunctionDescriptorUtils.resolve(
        lastEntry.myDescriptor,
        lastEntry.myLine,
        nextLocation.getFunctionName()
      );
    }
  }

  private static class TheRXResolvingSessionEntry {

    @Nullable
    private final TheRXFunctionDescriptor myDescriptor;

    private final int myLine;

    public TheRXResolvingSessionEntry(@Nullable final TheRXFunctionDescriptor descriptor, final int line) {
      myDescriptor = descriptor;
      myLine = line;
    }
  }
}
