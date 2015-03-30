package com.jetbrains.ther.debugger;

import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

public class TheRLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {

  @NotNull
  private final TheRDebugProcess myDebugProcess;

  public TheRLineBreakpointHandler(@NotNull TheRDebugProcess process) {
    super(TheRLineBreakpointType.class);
    myDebugProcess = process;
  }

  @Override
  public void registerBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
    myDebugProcess.registerBreakpoint(breakpoint);
  }

  @Override
  public void unregisterBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint, boolean temporary) {
    myDebugProcess.unregisterBreakpoint(breakpoint);
  }
}
