package com.jetbrains.ther.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.ConfigurationException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculator;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculatorImpl;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import com.jetbrains.ther.run.configuration.TheRRunConfigurationUtils;
import com.jetbrains.ther.run.run.TheRRunExecutionResultCalculator;
import org.jetbrains.annotations.NotNull;

import static java.lang.Boolean.parseBoolean;

public class TheRCommandLineState extends CommandLineState {

  @NotNull
  private static final String IO_ENV_KEY = "ther.debugger.io";

  @NotNull
  private final TheRRunConfiguration myRunConfiguration;

  public TheRCommandLineState(@NotNull final ExecutionEnvironment environment, @NotNull final TheRRunConfiguration runConfiguration) {
    super(environment);

    myRunConfiguration = runConfiguration;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    checkRunConfiguration();

    final ProcessHandler processHandler = startProcess(
      myRunConfiguration,
      TheRCommandLineCalculator.calculateCommandLine(
        TheRInterpreterService.getInstance().getInterpreterPath(),
        myRunConfiguration
      )
    );

    ProcessTerminatedListener.attach(processHandler, myRunConfiguration.getProject());

    return processHandler;
  }

  private void checkRunConfiguration() throws ExecutionException {
    try {
      TheRRunConfigurationUtils.checkConfiguration(myRunConfiguration);
    }
    catch (final ConfigurationException e) {
      throw new ExecutionException(e);
    }
  }

  @NotNull
  private ProcessHandler startProcess(@NotNull final TheRRunConfiguration runConfiguration,
                                      @NotNull final GeneralCommandLine commandLine) throws ExecutionException {
    return new TheRXProcessHandler(
      commandLine,
      createExecutionResultCalculator(),
      parseBoolean(runConfiguration.getEnvs().get(IO_ENV_KEY))
    );
  }

  @NotNull
  private TheRExecutionResultCalculator createExecutionResultCalculator() {
    if (getEnvironment().getExecutor().getId().equals(DefaultDebugExecutor.EXECUTOR_ID)) {
      return new TheRExecutionResultCalculatorImpl();
    }
    else {
      return new TheRRunExecutionResultCalculator();
    }
  }
}
