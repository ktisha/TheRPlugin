package com.jetbrains.ther.packages;

import com.intellij.execution.ExecutionException;
import org.jetbrains.annotations.NotNull;


public class TheRExecutionException extends ExecutionException {

  @NotNull private final String myCommand;
  @NotNull private final String myStdout;
  @NotNull private final String myStderr;
  private final int myExitCode;

  TheRExecutionException(@NotNull String message, @NotNull String command,
                         @NotNull String stdout, @NotNull String stderr, int exitCode) {
    super(message);
    myCommand = command;
    myStdout = stdout;
    myStderr = stderr;
    myExitCode = exitCode;
  }

  @NotNull
  public String getCommand() {
    return myCommand;
  }


  public int getExitCode() {
    return myExitCode;
  }

  @NotNull
  public String getStdout() {
    return myStdout;
  }

  @NotNull
  public String getStderr() {
    return myStderr;
  }
}
