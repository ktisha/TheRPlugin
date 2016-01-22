package com.jetbrains.ther.run.configuration;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TheRRunConfigurationEditor extends SettingsEditor<TheRRunConfiguration> {
  private TheRRunConfigurationForm myForm;

  public TheRRunConfigurationEditor(final TheRRunConfiguration configuration) {
    myForm = new TheRRunConfigurationForm(configuration);
  }

  @Override
  protected void resetEditorFrom(final TheRRunConfiguration config) {
    TheRRunConfiguration.copyParams(config, myForm);
  }

  @Override
  protected void applyEditorTo(final TheRRunConfiguration config) throws ConfigurationException {
    TheRRunConfiguration.copyParams(myForm, config);
  }

  @Override
  @NotNull
  protected JComponent createEditor() {
    return myForm.getPanel();
  }

  @Override
  protected void disposeEditor() {
    myForm = null;
  }
}
