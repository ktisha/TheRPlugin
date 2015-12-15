package com.jetbrains.ther.run;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

public class TheRConfigurationType extends ConfigurationTypeBase {

  public TheRConfigurationType() {
    super(
      "TheRConfigurationType",
      "R",
      "R run configuration",
      IconLoader.getIcon("/icons/Rlogo.png")
    );

    addFactory(new TheRConfigurationFactory(this));
  }

  @NotNull
  public static TheRConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(TheRConfigurationType.class);
  }

  private static class TheRConfigurationFactory extends ConfigurationFactory {

    public TheRConfigurationFactory(@NotNull final ConfigurationType configurationType) {
      super(configurationType);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
      return new TheRRunConfiguration(project, this);
    }

  }
}
