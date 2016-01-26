package com.jetbrains.ther.run.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.process.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.HashMap;
import com.jetbrains.ther.TheRFileType;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import com.jetbrains.ther.run.graphics.TheRGraphicsUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TheRRunCommandLineState extends CommandLineState {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRRunCommandLineState.class);

  public static final String GROUP_EXE_OPTIONS = "Exe Options";
  public static final String GROUP_SCRIPT = "Script";
  private final TheRRunConfiguration myConfig;

  public TheRRunCommandLineState(@NotNull final TheRRunConfiguration runConfiguration, @NotNull final ExecutionEnvironment env) {
    super(env);
    myConfig = runConfiguration;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    TheRGraphicsUtils.getGraphicsState(myConfig.getProject()).reset();

    final ProcessHandler processHandler = new KillableColoredProcessHandler(generateCommandLine());

    ProcessTerminatedListener.attach(processHandler);
    processHandler.addProcessListener(
      new ProcessAdapter() {
        @Override
        public void processTerminated(final ProcessEvent event) {
          TheRGraphicsUtils.getGraphicsState(myConfig.getProject()).refresh(false);
        }
      }
    );

    return processHandler;
  }

  public GeneralCommandLine generateCommandLine() throws ExecutionException {
    final GeneralCommandLine commandLine = new GeneralCommandLine();
    final String interpreterPath = getInterpreterPath();
    commandLine.setExePath(FileUtil.toSystemDependentName(interpreterPath));
    commandLine.setWorkDirectory(myConfig.getProject().getBasePath());
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
    final String scriptPath = myConfig.getScriptPath();
    if (!StringUtil.isEmptyOrSpaces(scriptPath)) {
      scriptParameters.addParameter(calculateUpdatedScriptPath(scriptPath));
    }
    final String scriptArgs = myConfig.getScriptArgs();
    if (!StringUtil.isEmptyOrSpaces(scriptArgs)) {
      scriptParameters.addParametersString("--args " + scriptArgs);
    }
  }

  protected void initEnvironment(@NotNull final GeneralCommandLine commandLine) {
    Map<String, String> env = myConfig.getEnvs();
    env = env == null ? new HashMap<String, String>() : new HashMap<String, String>(env);
    commandLine.getEnvironment().clear();
    commandLine.getEnvironment().putAll(env);
    commandLine.setPassParentEnvironment(myConfig.isPassParentEnvs());
  }

  @NotNull
  private String calculateUpdatedScriptPath(@NotNull final String originalScriptPath) {
    final List<String> initCommands = TheRGraphicsUtils.calculateInitCommands(myConfig);

    if (initCommands.isEmpty()) {
      return originalScriptPath;
    }

    try {
      return createUpdatedScript(originalScriptPath, initCommands).getAbsolutePath();
    }
    catch (final IOException e) {
      LOGGER.warn(e);

      return originalScriptPath;
    }
  }

  @NotNull
  private File createUpdatedScript(@NotNull final String originalScriptPath,
                                   @NotNull final List<String> initCommands) throws IOException {
    final File originalScriptFile = new File(originalScriptPath);
    final String originalScriptName = originalScriptFile.getName();

    final File updatedScriptFile = FileUtil.createTempFile(originalScriptName, "." + TheRFileType.INSTANCE.getDefaultExtension(), true);

    for (final String command : initCommands) {
      FileUtil.appendToFile(updatedScriptFile, command);
      FileUtil.appendToFile(updatedScriptFile, TheRDebugConstants.LINE_SEPARATOR);
    }

    FileUtil.appendToFile(updatedScriptFile, FileUtil.loadFile(originalScriptFile));

    return updatedScriptFile;
  }
}
