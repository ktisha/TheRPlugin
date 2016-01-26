package com.jetbrains.ther.run.debug;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculatorImpl;
import com.jetbrains.ther.run.TheRCommandLineState;
import com.jetbrains.ther.run.TheRXProcessHandler;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import org.jetbrains.annotations.NotNull;

import static java.lang.Boolean.parseBoolean;

public class TheRDebugCommandLineState extends TheRCommandLineState {

  @NotNull
  private static final String IO_ENV_KEY = "ther.debugger.io";

  public TheRDebugCommandLineState(@NotNull final ExecutionEnvironment environment,
                                   @NotNull final TheRRunConfiguration runConfiguration) {
    super(environment, runConfiguration);
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess(@NotNull final TheRRunConfiguration runConfiguration,
                                        @NotNull final GeneralCommandLine generalCommandLine) throws ExecutionException {
    return new TheRXProcessHandler(
      generalCommandLine,
      new TheRExecutionResultCalculatorImpl(),
      parseBoolean(runConfiguration.getEnvs().get(IO_ENV_KEY))
    );
  }
}
