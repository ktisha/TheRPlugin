package com.jetbrains.ther.run.configuration;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

public class TheRRunConfigurationType extends ConfigurationTypeBase {

  public TheRRunConfigurationType() {
    super(
      "TheRRunConfigurationType",
      "R",
      "R run configuration",
      IconLoader.getIcon("/icons/Rlogo.png")
    );

    addFactory(new TheRConfigurationFactory(this));
  }

  @NotNull
  public static TheRRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(TheRRunConfigurationType.class);
  }

  @NotNull
  public ConfigurationFactory getMainFactory() {
    return getConfigurationFactories()[0];
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
