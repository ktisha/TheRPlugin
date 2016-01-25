package com.jetbrains.ther.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.execution.ParametersListUtil;
import com.jetbrains.ther.debugger.executor.TheRProcessUtils;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// TODO [run][test]
public abstract class TheRCommandLineState extends CommandLineState {

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

  @NotNull
  protected abstract ProcessHandler startProcess(@NotNull final TheRRunConfiguration runConfiguration,
                                                 @NotNull final GeneralCommandLine generalCommandLine) throws ExecutionException;

  private void checkRunConfiguration() throws ExecutionException {
    if (StringUtil.isEmptyOrSpaces(myInterpreterPath)) {
      throw new ExecutionException("The R interpreter is not specified");
    }

    if (StringUtil.isEmptyOrSpaces(myRunConfiguration.getScriptPath())) {
      throw new ExecutionException("The R script is not specified");
    }

    if (StringUtil.isEmptyOrSpaces(myRunConfiguration.getWorkingDirectoryPath())) {
      throw new ExecutionException("The working directory is not specified");
    }
  }

  @NotNull
  private GeneralCommandLine calculateCommandLine() {
    return new GeneralCommandLine(calculateCommand())
      .withWorkDirectory(myRunConfiguration.getWorkingDirectoryPath())
      .withEnvironment(myRunConfiguration.getEnvs())
      .withParentEnvironmentType(myRunConfiguration.isPassParentEnvs() ? ParentEnvironmentType.CONSOLE : ParentEnvironmentType.NONE);
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
