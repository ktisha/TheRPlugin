package com.jetbrains.ther.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TheRConfigurationType implements ConfigurationType {

  private final TheRConfigurationFactory myFactory = new TheRConfigurationFactory(this);

  public static TheRConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(TheRConfigurationType.class);
  }

  private static class TheRConfigurationFactory extends ConfigurationFactory {
    protected TheRConfigurationFactory(ConfigurationType configurationType) {
      super(configurationType);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
      return new TheRRunConfiguration(project, this);
    }
  }

  @Override
  public String getDisplayName() {
    return "R script";
  }

  @Override
  public String getConfigurationTypeDescription() {
    return "The R run configuration";
  }

  @Override
  public Icon getIcon() {
    return IconLoader.getIcon("/icons/Rlogo.png");
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @Override
  @NotNull
  @NonNls
  public String getId() {
    return "TheRConfigurationType";
  }
}
