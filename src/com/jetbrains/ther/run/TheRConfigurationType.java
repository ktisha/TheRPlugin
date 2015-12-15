package com.jetbrains.ther.run;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
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

    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
      return new TheRRunConfiguration(project, this);
    }

    @Override
    public void configureBeforeRunTaskDefaults(@NotNull final Key<? extends BeforeRunTask> providerID, @NotNull final BeforeRunTask task) {
      if (task instanceof CompileStepBeforeRun.MakeBeforeRunTask) {
        task.setEnabled(false);
      }
    }
  }
}
