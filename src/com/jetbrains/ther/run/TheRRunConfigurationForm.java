package com.jetbrains.ther.run;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.RawCommandLineEditor;
import com.jetbrains.ther.TheRFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TheRRunConfigurationForm implements TheRRunConfigurationParams {

  private JPanel myRootPanel;
  private TextFieldWithBrowseButton myScriptTextField;
  private RawCommandLineEditor myScriptParametersTextField;
  private TextFieldWithBrowseButton myWorkingDirectoryTextField;
  private EnvironmentVariablesComponent myEnvsComponent;

  public TheRRunConfigurationForm(@NotNull final TheRRunConfiguration configuration) {
    final Project project = configuration.getProject();

    setupScriptTextField(project);
    setupScriptParametersTextField();
    setupWorkingDirectoryTextField(project);
  }

  @NotNull
  public JComponent getPanel() {
    return myRootPanel;
  }

  @NotNull
  @Override
  public String getScriptName() {
    return FileUtil.toSystemIndependentName(myScriptTextField.getText().trim());
  }

  @Override
  public void setScriptName(@Nullable final String scriptName) {
    myScriptTextField.setText(scriptName == null ? "" : FileUtil.toSystemDependentName(scriptName));
  }

  @NotNull
  @Override
  public String getScriptParameters() {
    return myScriptParametersTextField.getText().trim();
  }

  @Override
  public void setScriptParameters(@NotNull final String scriptParameters) {
    myScriptParametersTextField.setText(scriptParameters);
  }

  private void setupScriptTextField(@NotNull final Project project) {
    final ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> listener =
      new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>(
        "Select Script",
        "",
        myScriptTextField,
        project,
        FileChooserDescriptorFactory.createSingleFileDescriptor(TheRFileType.INSTANCE),
        TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
      );

    myScriptTextField.addActionListener(listener);
  }

  private void setupScriptParametersTextField() {
    myScriptParametersTextField.setDialogCaption("Script Parameters");
  }

  private void setupWorkingDirectoryTextField(@NotNull final Project project) {
    myWorkingDirectoryTextField.addBrowseFolderListener(
      "Select Working Directory",
      "",
      project,
      FileChooserDescriptorFactory.createSingleFolderDescriptor()
    );
  }
}
