package com.jetbrains.ther.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TheRDebugProcess extends XDebugProcess {

  @NotNull
  private final TheRDebugger myDebugger;

  @NotNull
  private final Set<Integer> myBreakpointLines;

  private int myExecutedLines;

  public TheRDebugProcess(@NotNull XDebugSession session, @NotNull String interpreterPath, @NotNull String filePath)
    throws ExecutionException {
    super(session);

    try {
      myDebugger = new TheRDebugger(interpreterPath, filePath);
    }
    catch (IOException | InterruptedException e) {
      throw new ExecutionException(e); // TODO
    }

    myBreakpointLines = new HashSet<>();
    myExecutedLines = 0;
  }

  @NotNull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return new TheRDebuggerEditorsProvider();
  }

  @NotNull
  @Override
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return new XBreakpointHandler[]{new TheRLineBreakpointHandler(this)};
  }

  @Override
  public void sessionInitialized() {
    resume();
  }

  @Override
  public void startStepOver() {
    try {
      myExecutedLines += myDebugger.executeInstruction(); // TODO -1 case
    }
    catch (IOException | InterruptedException e) {
      // TODO
    }
  }

  @Override
  public void startStepInto() {
    // TODO
  }

  @Override
  public void startStepOut() {
    // TODO
  }

  @Override
  public void resume() {
    try {
      while (!myBreakpointLines.contains(myExecutedLines)) { // TODO before line
        myExecutedLines += myDebugger.executeInstruction(); // TODO -1 case
      }
    }
    catch (IOException | InterruptedException e) {
      // TODO
    }
  }

  @Override
  public void runToPosition(@NotNull XSourcePosition position) {
    // TODO
  }

  @Override
  public void stop() {
    // TODO
  }

  public void registerBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myBreakpointLines.add(breakpoint.getLine());
  }

  public void unregisterBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myBreakpointLines.remove(breakpoint.getLine());
  }
}
