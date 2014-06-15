package com.jetbrains.ther.run;

import org.jetbrains.annotations.NotNull;

public interface TheRRunConfigurationParams {
  String getScriptName();

  void setScriptName(@NotNull final String scriptName);

  String getScriptParameters();

  void setScriptParameters(@NotNull final String scriptParameters);
}

