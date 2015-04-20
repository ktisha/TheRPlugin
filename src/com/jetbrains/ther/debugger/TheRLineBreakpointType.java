package com.jetbrains.ther.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.ther.TheRFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRLineBreakpointType extends XLineBreakpointType<XBreakpointProperties> {

  @NotNull
  private static final String ID = "the-r-line";

  @NotNull
  private static final String TITLE = "The R breakpoints";

  @NotNull
  private final TheRDebuggerEditorsProvider myProvider = new TheRDebuggerEditorsProvider();

  public TheRLineBreakpointType() {
    super(ID, TITLE);
  }

  @Nullable
  @Override
  public XBreakpointProperties createBreakpointProperties(@NotNull final VirtualFile file, final int line) {
    return null;
  }

  @Override
  public boolean canPutAt(@NotNull final VirtualFile file, final int line, @NotNull final Project project) {
    final String defaultExtension = TheRFileType.INSTANCE.getDefaultExtension();
    final String extension = file.getExtension();

    return defaultExtension.equalsIgnoreCase(extension);
  }

  @Nullable
  @Override
  public XDebuggerEditorsProvider getEditorsProvider(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint,
                                                     @NotNull final Project project) {
    return myProvider;
  }
}
