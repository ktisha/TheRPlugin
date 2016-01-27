package com.jetbrains.ther.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.execution.ParametersListUtil;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculator;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculatorImpl;
import com.jetbrains.ther.debugger.executor.TheRProcessUtils;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import com.jetbrains.ther.run.configuration.TheRRunConfigurationUtils;
import com.jetbrains.ther.run.run.TheRRunExecutionResultCalculator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.parseBoolean;

// TODO [run][test]
public class TheRCommandLineState extends CommandLineState {

  @NotNull
  private static final String IO_ENV_KEY = "ther.debugger.io";

  @NotNull
  private final String myInterpreterPath;

  @NotNull
  private final TheRRunConfiguration myRunConfiguration;

  public TheRCommandLineState(@NotNull final ExecutionEnvironment environment, @NotNull final TheRRunConfiguration runConfiguration) {
    super(environment);

    myInterpreterPath = TheRInterpreterService.getInstance().getInterpreterPath();
    myRunConfiguration = runConfiguration;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    checkRunConfiguration();

    final ProcessHandler processHandler = startProcess(myRunConfiguration, calculateCommandLine());

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
                                      @NotNull final GeneralCommandLine generalCommandLine) throws ExecutionException {
    return new TheRXProcessHandler(
      generalCommandLine,
      createExecutionResultCalculator(),
      parseBoolean(runConfiguration.getEnvs().get(IO_ENV_KEY))
    );
  }

  @NotNull
  private GeneralCommandLine calculateCommandLine() {
    return new GeneralCommandLine(calculateCommand())
      .withWorkDirectory(myRunConfiguration.getWorkingDirectoryPath())
      .withEnvironment(myRunConfiguration.getEnvs())
      .withParentEnvironmentType(myRunConfiguration.isPassParentEnvs() ? ParentEnvironmentType.CONSOLE : ParentEnvironmentType.NONE);
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

  @NotNull
  private List<String> calculateCommand() {
    final List<String> command = new ArrayList<String>();

    command.add(FileUtil.toSystemDependentName(myInterpreterPath));
    command.addAll(TheRProcessUtils.getStartOptions());

    final String scriptArgs = myRunConfiguration.getScriptArgs();

    if (!StringUtil.isEmptyOrSpaces(scriptArgs)) {
      command.add("--args");
      command.addAll(ParametersListUtil.parse(scriptArgs));
    }

    return command;
  }
}
