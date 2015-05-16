package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.LineSeparator;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.ther.debugger.TheRDebugger;
import com.jetbrains.ther.debugger.data.TheROutput;
import com.jetbrains.ther.debugger.data.TheRStackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheRXDebugProcess extends XDebugProcess {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRXDebugProcess.class);

  @NotNull
  private final TheRDebugger myDebugger;

  @NotNull
  private final Map<Integer, XLineBreakpoint<XBreakpointProperties>> myBreakpoints; // TODO use XSourcePosition as a key

  @NotNull
  private final TheRLocationResolver myLocationResolver;

  @NotNull
  private final ConsoleView myConsole;

  public TheRXDebugProcess(@NotNull final XDebugSession session, @NotNull final TheRDebugger debugger)
    throws ExecutionException {
    super(session);

    myDebugger = debugger;

    myBreakpoints = new HashMap<Integer, XLineBreakpoint<XBreakpointProperties>>();
    myLocationResolver = new TheRLocationResolver();

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
      final boolean executed = myDebugger.executeInstruction();

      if (!executed) {
        getSession().stop();

        return;
      }

      handleInterpreterOutput();

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
    // TODO impl
  }

  @Override
  public void startStepOut() {
    // TODO impl
  }

  @Override
  public void resume() {
    try {
      while ((!myBreakpoints.containsKey(getCurrentDebuggerLocation()))) {
        final boolean executed = myDebugger.executeInstruction();

        if (!executed) {
          getSession().stop();

          return;
        }

        handleInterpreterOutput();
      }

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
    // TODO impl
  }

  @Override
  public void stop() {
    myDebugger.stop();
  }

  public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myBreakpoints.put(breakpoint.getLine(), breakpoint);
  }

  public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myBreakpoints.remove(breakpoint.getLine());
  }

  private void handleInterpreterOutput() {
    final TheROutput output = myDebugger.getOutput();

    printToConsole(output.getNormalOutput(), ConsoleViewContentType.NORMAL_OUTPUT);
    printToConsole(output.getErrorOutput(), ConsoleViewContentType.ERROR_OUTPUT);
  }

  private void updateDebugInformation() {
    final XDebugSession session = getSession();
    final XLineBreakpoint<XBreakpointProperties> breakpoint = myBreakpoints.get(getCurrentDebuggerLocation());
    final TheRXSuspendContext suspendContext = new TheRXSuspendContext(myDebugger.getStack(), myLocationResolver);

    if (breakpoint != null) {
      if (!session
        .breakpointReached(breakpoint, null, suspendContext)) { // second argument is printed to console when breakpoint is reached
        resume();
      }
    }
    else {
      session.positionReached(suspendContext);
    }
  }

  private int getCurrentDebuggerLocation() {
    final List<TheRStackFrame> stack = myDebugger.getStack();

    return myLocationResolver.resolve(stack.get(stack.size() - 1).getLocation()).getLine();
  }

  private void printToConsole(@Nullable final String text, @NotNull final ConsoleViewContentType type) {
    if (text != null) {
      myConsole.print(text, type);
      myConsole.print(LineSeparator.getSystemLineSeparator().getSeparatorString(), type);
    }
  }
}
