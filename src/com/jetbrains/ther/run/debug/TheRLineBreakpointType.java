package com.jetbrains.ther.run.debug;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase;
import org.jetbrains.annotations.NotNull;

public class TheRLineBreakpointType extends XLineBreakpointTypeBase {

  @NotNull
  private static final String ID = "the-r-line";

  @NotNull
  private static final String TITLE = "The R Breakpoints";

  public TheRLineBreakpointType() {
    super(ID, TITLE, new TheREditorsProvider());
  }

  @Override
  public boolean canPutAt(@NotNull final VirtualFile file, final int line, @NotNull final Project project) {
    return TheRLineBreakpointUtils.canPutAt(project, file, line);
  }
}
