package com.jetbrains.ther.run.configuration;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBLabel;
import com.jetbrains.ther.TheRFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

public class TheRRunConfigurationForm implements TheRRunConfigurationParams {

  private JPanel myRootPanel;

  private JBLabel myScriptLabel;
  private TextFieldWithBrowseButton myScriptTextField;

  private JBLabel myScriptArgsLabel;
  private RawCommandLineEditor myScriptArgsTextField;

  private JBLabel myWorkingDirectoryLabel;
  private TextFieldWithBrowseButton myWorkingDirectoryTextField;

  private EnvironmentVariablesComponent myEnvsComponent;

  public TheRRunConfigurationForm(@NotNull final TheRRunConfiguration configuration) {
    final Project project = configuration.getProject();

    setupScriptTextField(project);
    setupScriptArgsTextField();
    setupWorkingDirectoryTextField(project);

    myScriptLabel.setAnchor(myEnvsComponent.getLabel());
    myScriptArgsLabel.setAnchor(myEnvsComponent.getLabel());
    myWorkingDirectoryLabel.setAnchor(myEnvsComponent.getLabel());
  }

  @NotNull
  public JComponent getPanel() {
    return myRootPanel;
  }

  @NotNull
  @Override
  public String getScriptPath() {
    return getPath(myScriptTextField);
  }

  @Override
  public void setScriptPath(@NotNull final String scriptPath) {
    setPath(scriptPath, myScriptTextField);
  }

  @NotNull
  @Override
  public String getScriptArgs() {
    return myScriptArgsTextField.getText().trim();
  }

  @Override
  public void setScriptArgs(@NotNull final String scriptArgs) {
    myScriptArgsTextField.setText(scriptArgs);
  }

  @NotNull
  @Override
  public String getWorkingDirectory() {
    return getPath(myWorkingDirectoryTextField);
  }

  @Override
  public void setWorkingDirectory(@NotNull final String workingDirectory) {
    setPath(workingDirectory, myWorkingDirectoryTextField);
  }

  @Override
  public boolean isPassParentEnvs() {
    return myEnvsComponent.isPassParentEnvs();
  }

  @Override
  public void setPassParentEnvs(final boolean passParentEnvs) {
    myEnvsComponent.setPassParentEnvs(passParentEnvs);
  }

  @NotNull
  @Override
  public Map<String, String> getEnvs() {
    return myEnvsComponent.getEnvs();
  }

  @Override
  public void setEnvs(@NotNull final Map<String, String> envs) {
    myEnvsComponent.setEnvs(envs);
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
      ) {
        @Override
        protected void onFileChosen(@NotNull final VirtualFile chosenFile) {
          super.onFileChosen(chosenFile);

          if (StringUtil.isEmptyOrSpaces(getWorkingDirectory())) {
            setWorkingDirectory(chosenFile.getParent().getPath());
          }
        }
      };

    myScriptTextField.addActionListener(listener);
  }

  private void setupScriptArgsTextField() {
    myScriptArgsTextField.setDialogCaption("Script Args");
  }

  private void setupWorkingDirectoryTextField(@NotNull final Project project) {
    myWorkingDirectoryTextField.addBrowseFolderListener(
      "Select Working Directory",
      "",
      project,
      FileChooserDescriptorFactory.createSingleFolderDescriptor()
    );
  }

  @NotNull
  private String getPath(@NotNull final TextFieldWithBrowseButton textField) {
    return FileUtil.toSystemIndependentName(textField.getText().trim());
  }

  private void setPath(@NotNull final String path, @NotNull final TextFieldWithBrowseButton textField) {
    textField.setText(FileUtil.toSystemDependentName(path));
  }
}
