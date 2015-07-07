package com.jetbrains.ther.xdebugger;

import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

class TheRXLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {

  @NotNull
  private final TheRXDebugProcess myDebugProcess;

  public TheRXLineBreakpointHandler(@NotNull final TheRXDebugProcess process) {
    super(TheRXLineBreakpointType.class);

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
