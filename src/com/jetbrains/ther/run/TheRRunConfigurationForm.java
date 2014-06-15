package com.jetbrains.ther.run;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TheRRunConfigurationForm implements TheRRunConfigurationParams, PanelWithAnchor {
  private JPanel myRootPanel;
  private TextFieldWithBrowseButton myScriptTextField;
  private RawCommandLineEditor myScriptParametersTextField;
  private JBLabel myScriptParametersLabel;
  private JComponent anchor;

  public TheRRunConfigurationForm(TheRRunConfiguration configuration) {
    final Project project = configuration.getProject();
    FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
      @Override
      public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
        return file.isDirectory() || file.getExtension() == null || Comparing.equal(file.getExtension(), "r");
      }
    };
    ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> listener =
      new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>("Select Script", "", myScriptTextField, project,
                                                                           chooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
    myScriptTextField.addActionListener(listener);
  }

  public JComponent getPanel() {
    return myRootPanel;
  }

  @Override
  public String getScriptName() {
    return FileUtil.toSystemIndependentName(myScriptTextField.getText().trim());
  }

  @Override
  public void setScriptName(@Nullable final String scriptName) {
    myScriptTextField.setText(scriptName == null ? "" : FileUtil.toSystemDependentName(scriptName));
  }

  @Override
  public String getScriptParameters() {
    return myScriptParametersTextField.getText().trim();
  }

  @Override
  public void setScriptParameters(@NotNull String scriptParameters) {
    myScriptParametersTextField.setText(scriptParameters);
  }

  @Override
  public JComponent getAnchor() {
    return anchor;
  }

  @Override
  public void setAnchor(JComponent anchor) {
    this.anchor = anchor;
    myScriptParametersLabel.setAnchor(anchor);
  }
}
