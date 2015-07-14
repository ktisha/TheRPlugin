package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.ther.debugger.TheRDebugger;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRStackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

// TODO [xdbg][test]
class TheRXDebugProcess extends XDebugProcess {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRXDebugProcess.class);

  @NotNull
  private final TheRDebugger myDebugger;

  @NotNull
  private final TheRLocationResolver myLocationResolver;

  @NotNull
  private final TheROutputBuffer myOutputBuffer;

  @NotNull
  private final Map<XSourcePositionWrapper, XLineBreakpoint<XBreakpointProperties>> myBreakpoints;

  @NotNull
  private final Set<XSourcePositionWrapper> myTempBreakpoints;

  @NotNull
  private final ConsoleView myConsole;

  public TheRXDebugProcess(@NotNull final XDebugSession session,
                           @NotNull final TheRDebugger debugger,
                           @NotNull final TheRLocationResolver locationResolver,
                           @NotNull final TheROutputBuffer outputBuffer) {
    super(session);

    myDebugger = debugger;
    myLocationResolver = locationResolver;
    myOutputBuffer = outputBuffer;

    myBreakpoints = new HashMap<XSourcePositionWrapper, XLineBreakpoint<XBreakpointProperties>>();
    myTempBreakpoints = new HashSet<XSourcePositionWrapper>();

    myConsole = (ConsoleView)super.createConsole();
  }

  @NotNull
  @Override
  public ExecutionConsole createConsole() {
    return myConsole;
  }

  @NotNull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return new TheRXDebuggerEditorsProvider();
  }

  @NotNull
  @Override
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return new XBreakpointHandler[]{new TheRXLineBreakpointHandler(this)};
  }

  @Override
  public void sessionInitialized() {
    resume();
  }

  @Override
  public void startStepOver() {
    try {
      final int targetDepth = myDebugger.getStack().size();

      do {
        if (!advance()) return;
      }
      while (!isBreakpoint() && myDebugger.getStack().size() > targetDepth);

      updateDebugInformation();
    }
    catch (final IOException e) {
      LOGGER.error(e);
    }
    catch (final InterruptedException e) {
      LOGGER.error(e);
    }
  }

  @Override
  public void startStepInto() {
    try {
      if (!advance()) return;

      updateDebugInformation();
    }
    catch (final IOException e) {
      LOGGER.error(e);
    }
    catch (final InterruptedException e) {
      LOGGER.error(e);
    }
  }

  @Override
  public void startStepOut() {
    try {
      final int targetDepth = myDebugger.getStack().size() - 1;

      do {
        if (!advance()) return;
      }
      while (!isBreakpoint() && myDebugger.getStack().size() != targetDepth);

      updateDebugInformation();
    }
    catch (final IOException e) {
      LOGGER.error(e);
    }
    catch (final InterruptedException e) {
      LOGGER.error(e);
    }
  }

  @Override
  public void resume() {
    try {
      do {
        if (!advance()) return;
      }
      while (!isBreakpoint());

      updateDebugInformation();
    }
    catch (final IOException e) {
      LOGGER.error(e);
    }
    catch (final InterruptedException e) {
      LOGGER.error(e);
    }
  }

  @Override
  public void runToPosition(@NotNull final XSourcePosition position) {
    myTempBreakpoints.add(new XSourcePositionWrapper(position));

    resume();
  }

  @Override
  public void stop() {
    myDebugger.stop();
  }

  public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myBreakpoints.put(
      new XSourcePositionWrapper(breakpoint.getSourcePosition()),
      breakpoint
    );
  }

  public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myBreakpoints.remove(
      new XSourcePositionWrapper(breakpoint.getSourcePosition())
    );
  }

  private boolean advance() throws IOException, InterruptedException {
    final boolean executed = myDebugger.advance();

    printInterpreterOutput();

    if (!executed) {
      getSession().stop();
    }

    return executed;
  }

  private void updateDebugInformation() {
    final XSourcePositionWrapper wrapper = new XSourcePositionWrapper(getCurrentDebuggerLocation());
    final XLineBreakpoint<XBreakpointProperties> breakpoint = myBreakpoints.get(wrapper);

    final XDebugSession session = getSession();
    final TheRXSuspendContext suspendContext = new TheRXSuspendContext(myDebugger.getStack(), myLocationResolver);

    if (breakpoint != null) {
      if (!session
        .breakpointReached(breakpoint, null, suspendContext)) { // second argument is printed to console when breakpoint is reached
        resume();
      }
    }
    else {
      session.positionReached(suspendContext);

      myTempBreakpoints.remove(wrapper);
    }
  }

  private boolean isBreakpoint() {
    final XSourcePositionWrapper wrapper = new XSourcePositionWrapper(getCurrentDebuggerLocation());

    return myBreakpoints.containsKey(wrapper) || myTempBreakpoints.contains(wrapper);
  }

  private void printInterpreterOutput() {
    final Queue<String> messages = myOutputBuffer.getMessages();

    while (!messages.isEmpty()) {
      final String message = messages.poll();

      if (message != null) {
        myConsole.print(message, ConsoleViewContentType.NORMAL_OUTPUT);
        myConsole.print(
          TheRDebugConstants.LINE_SEPARATOR,
          ConsoleViewContentType.NORMAL_OUTPUT
        );
      }
    }
  }

  @NotNull
  private XSourcePosition getCurrentDebuggerLocation() {
    final List<TheRStackFrame> stack = myDebugger.getStack();

    return myLocationResolver.resolve(stack.get(stack.size() - 1).getLocation());
  }

  private static class XSourcePositionWrapper {

    @NotNull
    private final XSourcePosition myPosition;

    private XSourcePositionWrapper(@NotNull final XSourcePosition position) {
      myPosition = position;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
      if (o == this) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final XSourcePositionWrapper wrapper = (XSourcePositionWrapper)o;

      return myPosition.getLine() == wrapper.myPosition.getLine() &&
             myPosition.getFile().getPath().equals(wrapper.myPosition.getFile().getPath());
    }

    @Override
    public int hashCode() {
      return 31 * myPosition.getLine() + myPosition.getFile().getPath().hashCode();
    }
  }
}
