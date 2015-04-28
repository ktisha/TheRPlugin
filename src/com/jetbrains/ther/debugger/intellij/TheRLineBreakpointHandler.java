package com.jetbrains.ther.debugger.intellij;

import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

public class TheRLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {

  @NotNull
  private final TheRDebugProcess myDebugProcess;

  public TheRLineBreakpointHandler(@NotNull final TheRDebugProcess process) {
    super(TheRLineBreakpointType.class);

    myDebugProcess = process;
  }

  @Override
  public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myDebugProcess.registerBreakpoint(breakpoint);
  }

  @Override
  public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint, final boolean temporary) {
    myDebugProcess.unregisterBreakpoint(breakpoint);
  }
}
