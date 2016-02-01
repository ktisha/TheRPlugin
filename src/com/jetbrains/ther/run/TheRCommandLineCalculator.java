package com.jetbrains.ther.run;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.execution.ParametersListUtil;
import com.jetbrains.ther.debugger.data.TheRInterpreterParameters;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class TheRCommandLineCalculator {

  @NotNull
  public static GeneralCommandLine calculateCommandLine(@NotNull final String interpreterPath,
                                                        @NotNull final TheRRunConfiguration runConfiguration) {
    return new GeneralCommandLine(calculateCommand(interpreterPath, runConfiguration))
      .withWorkDirectory(runConfiguration.getWorkingDirectoryPath())
      .withEnvironment(runConfiguration.getEnvs())
      .withParentEnvironmentType(runConfiguration.isPassParentEnvs() ? ParentEnvironmentType.CONSOLE : ParentEnvironmentType.NONE);
  }

  @NotNull
  private static List<String> calculateCommand(@NotNull final String interpreterPath,
                                               @NotNull final TheRRunConfiguration runConfiguration) {
    final List<String> command = new ArrayList<String>();

    command.add(FileUtil.toSystemDependentName(interpreterPath));
    command.addAll(TheRInterpreterParameters.DEFAULT_PARAMETERS);

    final String scriptArgs = runConfiguration.getScriptArgs();

    if (!StringUtil.isEmptyOrSpaces(scriptArgs)) {
      command.add(TheRInterpreterParameters.ARGS_PARAMETER);
      command.addAll(ParametersListUtil.parse(scriptArgs));
    }

    return command;
  }
}
