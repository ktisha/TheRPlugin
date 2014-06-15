package com.jetbrains.ther.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.HashMap;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TheRCommandLineState extends CommandLineState {
  public static final String GROUP_EXE_OPTIONS = "Exe Options";
  public static final String GROUP_SCRIPT = "Script";
  private final TheRRunConfiguration myConfig;

  public TheRCommandLineState(@NotNull final TheRRunConfiguration runConfiguration, @NotNull final ExecutionEnvironment env) {
    super(env);
    myConfig = runConfiguration;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    final GeneralCommandLine commandLine = generateCommandLine();
    final ProcessHandler processHandler = TheRProcessHandler.createProcessHandler(commandLine);
    ProcessTerminatedListener.attach(processHandler);

    return processHandler;
  }

  public GeneralCommandLine generateCommandLine() throws ExecutionException {
    final GeneralCommandLine commandLine = new GeneralCommandLine();
    final String interpreterPath = getInterpreterPath();
    commandLine.setExePath(FileUtil.toSystemDependentName(interpreterPath));

    buildCommandLineParameters(commandLine);
    initEnvironment(commandLine);
    return commandLine;
  }


  protected String getInterpreterPath() throws ExecutionException {
    final String interpreterPath = TheRInterpreterService.getInstance().getInterpreterPath();
    if (interpreterPath == null) {
      throw new ExecutionException("Cannot find R interpreter for this run configuration");
    }
    return interpreterPath;
  }

  protected void buildCommandLineParameters(@NotNull final GeneralCommandLine commandLine) {
    final ParametersList parametersList = commandLine.getParametersList();
    final ParamsGroup exeOptions = parametersList.addParamsGroup(GROUP_EXE_OPTIONS);
    exeOptions.addParametersString("--slave");
    exeOptions.addParametersString("-f");

    final ParamsGroup scriptParameters = parametersList.addParamsGroup(GROUP_SCRIPT);
    final String scriptName = myConfig.getScriptName();
    if (!StringUtil.isEmptyOrSpaces(scriptName)) {
      scriptParameters.addParameter(scriptName);
    }
  }

  protected void initEnvironment(@NotNull final GeneralCommandLine commandLine) {
    Map<String, String> env = myConfig.getEnvs();
    env = env == null ? new HashMap<String, String>() : new HashMap<String, String>(env);
    commandLine.getEnvironment().clear();
    commandLine.getEnvironment().putAll(env);
    commandLine.setPassParentEnvironment(myConfig.isPassParentEnvs());

  }
}
