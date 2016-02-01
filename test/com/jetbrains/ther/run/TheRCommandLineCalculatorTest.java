package com.jetbrains.ther.run;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType;
import com.jetbrains.ther.debugger.data.TheRInterpreterParameters;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TheRCommandLineCalculatorTest {

  @NotNull
  private static final String INTERPRETER_PATH = "/usr/bin/R";

  @NotNull
  private static final String WORKING_DIRECTORY_PATH = "/home/user";

  @NotNull
  private static final Map<String, String> ENVS = calculateEnvs();

  @Test
  public void ordinary() {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    final String arg1 = "ARG1";
    final String arg2 = "ARG2";

    when(runConfiguration.getScriptArgs()).thenReturn(arg1 + " " + arg2);
    when(runConfiguration.getWorkingDirectoryPath()).thenReturn(WORKING_DIRECTORY_PATH);
    when(runConfiguration.getEnvs()).thenReturn(ENVS);
    when(runConfiguration.isPassParentEnvs()).thenReturn(true);

    final GeneralCommandLine commandLine = TheRCommandLineCalculator.calculateCommandLine(INTERPRETER_PATH, runConfiguration);

    assertEquals(
      Arrays.asList(
        TheRInterpreterParameters.NO_SAVE_PARAMETER,
        TheRInterpreterParameters.QUIET_PARAMETER,
        TheRInterpreterParameters.ARGS_PARAMETER,
        arg1,
        arg2
      ),
      commandLine.getParametersList().getList()
    );
    assertEquals(WORKING_DIRECTORY_PATH, commandLine.getWorkDirectory().getAbsolutePath());
    assertEquals(ENVS, commandLine.getEnvironment());
    assertEquals(ParentEnvironmentType.CONSOLE, commandLine.getParentEnvironmentType());
  }

  @Test
  public void disableScriptArgs() {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getScriptArgs()).thenReturn("");
    when(runConfiguration.getWorkingDirectoryPath()).thenReturn(WORKING_DIRECTORY_PATH);
    when(runConfiguration.getEnvs()).thenReturn(ENVS);
    when(runConfiguration.isPassParentEnvs()).thenReturn(true);

    final GeneralCommandLine commandLine = TheRCommandLineCalculator.calculateCommandLine(INTERPRETER_PATH, runConfiguration);

    assertEquals(TheRInterpreterParameters.DEFAULT_PARAMETERS, commandLine.getParametersList().getList());
    assertEquals(WORKING_DIRECTORY_PATH, commandLine.getWorkDirectory().getAbsolutePath());
    assertEquals(ENVS, commandLine.getEnvironment());
    assertEquals(ParentEnvironmentType.CONSOLE, commandLine.getParentEnvironmentType());
  }

  @Test
  public void disableParentEnvs() {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getScriptArgs()).thenReturn("");
    when(runConfiguration.getWorkingDirectoryPath()).thenReturn(WORKING_DIRECTORY_PATH);
    when(runConfiguration.getEnvs()).thenReturn(ENVS);
    when(runConfiguration.isPassParentEnvs()).thenReturn(false);

    final GeneralCommandLine commandLine = TheRCommandLineCalculator.calculateCommandLine(INTERPRETER_PATH, runConfiguration);

    assertEquals(TheRInterpreterParameters.DEFAULT_PARAMETERS, commandLine.getParametersList().getList());
    assertEquals(WORKING_DIRECTORY_PATH, commandLine.getWorkDirectory().getAbsolutePath());
    assertEquals(ENVS, commandLine.getEnvironment());
    assertEquals(ParentEnvironmentType.NONE, commandLine.getParentEnvironmentType());
  }

  @NotNull
  private static Map<String, String> calculateEnvs() {
    final Map<String, String> envs = new HashMap<String, String>();
    envs.put("K1", "V1");
    envs.put("K2", "V2");

    return envs;
  }
}