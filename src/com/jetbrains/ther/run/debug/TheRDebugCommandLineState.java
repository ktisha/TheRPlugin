package com.jetbrains.ther.run.debug;

import com.intellij.execution.runners.ExecutionEnvironment;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculator;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculatorImpl;
import com.jetbrains.ther.run.TheRCommandLineState;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import org.jetbrains.annotations.NotNull;

public class TheRDebugCommandLineState extends TheRCommandLineState {

  public TheRDebugCommandLineState(@NotNull final ExecutionEnvironment environment,
                                   @NotNull final TheRRunConfiguration runConfiguration) {
    super(environment, runConfiguration);
  }

  @NotNull
  @Override
  protected TheRExecutionResultCalculator getExecutionResultCalculator() {
    return new TheRExecutionResultCalculatorImpl();
  }
}
