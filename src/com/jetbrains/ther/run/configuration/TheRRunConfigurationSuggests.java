package com.jetbrains.ther.run.configuration;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.TheRFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class TheRRunConfigurationSuggests {

  @Nullable
  public static String suggestedName(@NotNull final TheRRunConfigurationParams runConfigurationParams) {
    final String scriptPath = runConfigurationParams.getScriptPath();

    if (StringUtil.isEmptyOrSpaces(scriptPath)) return null;

    final String name = new File(scriptPath).getName();
    final String dotAndExtension = "." + TheRFileType.INSTANCE.getDefaultExtension();

    if (name.length() > dotAndExtension.length() && StringUtil.endsWithIgnoreCase(name, dotAndExtension)) {
      return name.substring(0, name.length() - dotAndExtension.length());
    }

    return name;
  }

  @Nullable
  public static String suggestedWorkingDirectoryPath(@NotNull final TheRRunConfigurationParams runConfigurationParams) {
    final String scriptPath = runConfigurationParams.getScriptPath();

    if (StringUtil.isEmptyOrSpaces(scriptPath)) return null;

    return new File(scriptPath).getParent();
  }

  public static void setSuggestedWorkingDirectoryPathIfNotSpecified(@NotNull final TheRRunConfigurationParams runConfigurationParams) {
    if (!StringUtil.isEmptyOrSpaces(runConfigurationParams.getWorkingDirectoryPath())) {
      return;
    }

    final String suggestedWorkingDirectoryPath = TheRRunConfigurationSuggests.suggestedWorkingDirectoryPath(runConfigurationParams);

    if (suggestedWorkingDirectoryPath != null) {
      runConfigurationParams.setWorkingDirectoryPath(suggestedWorkingDirectoryPath);
    }
  }
}
