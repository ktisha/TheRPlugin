package com.jetbrains.ther.run;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface TheRRunConfigurationParams {

  @NotNull
  String getScriptPath();

  void setScriptPath(@NotNull final String scriptPath);

  @NotNull
  String getScriptParameters();

  void setScriptParameters(@NotNull final String scriptParameters);

  @NotNull
  String getWorkingDirectory();

  void setWorkingDirectory(@NotNull final String workingDirectory);

  boolean isPassParentEnvs();

  void setPassParentEnvs(final boolean passParentEnvs);

  @NotNull
  Map<String, String> getEnvs();

  void setEnvs(@NotNull final Map<String, String> envs);
}

