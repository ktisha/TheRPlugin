package com.jetbrains.ther.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableColoredProcessHandler;
import org.jetbrains.annotations.NotNull;

public class TheRProcessHandler extends KillableColoredProcessHandler {
  protected TheRProcessHandler(@NotNull Process process, @NotNull GeneralCommandLine commandLine) {
    super(process, commandLine.getCommandLineString());
  }

  @Override
  protected boolean shouldDestroyProcessRecursively() {
    return true;
  }

  public static TheRProcessHandler createProcessHandler(@NotNull final GeneralCommandLine commandLine)
    throws ExecutionException {
    final Process p = commandLine.createProcess();
    return new TheRProcessHandler(p, commandLine);
  }
}
