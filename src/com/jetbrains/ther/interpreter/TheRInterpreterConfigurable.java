package com.jetbrains.ther.interpreter;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class TheRInterpreterConfigurable implements SearchableConfigurable, Configurable.NoScroll{
  private JPanel myMainPanel;
  private final Project myProject;
  private final TextFieldWithBrowseButton myInterpreterField;
  private final TextFieldWithBrowseButton mySourcesField;

  TheRInterpreterConfigurable(Project project) {
    myProject = project;

    final GridBagLayout layout = new GridBagLayout();
    myMainPanel = new JPanel(layout);
    final JLabel interpreterLabel = new JLabel("Interpreter:");
    final JLabel sourcesLabel = new JLabel("Sources:");
    myInterpreterField = new TextFieldWithBrowseButton();
    mySourcesField = new TextFieldWithBrowseButton();
    final FileChooserDescriptor interpreterDescriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
    myInterpreterField.addBrowseFolderListener("Choose interpreter path", "Choose interpreter path", myProject, interpreterDescriptor);
    final FileChooserDescriptor sourcesDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    mySourcesField.addBrowseFolderListener("Choose R sources folder", "Choose R sources folder", myProject, sourcesDescriptor);

    final GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(2,2,2,2);
    c.anchor = GridBagConstraints.NORTH;

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.;
    c.weighty = 0.;
    final JPanel interpreterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    interpreterPanel.add(interpreterLabel);
    myMainPanel.add(interpreterPanel, c);

    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1.;
    myMainPanel.add(myInterpreterField, c);

    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.;
    c.weighty = 1.;
    final JPanel sourcesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    sourcesPanel.add(sourcesLabel);
    myMainPanel.add(sourcesPanel, c);

    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 1.;
    myMainPanel.add(mySourcesField, c);
  }

  @NotNull
  @Override
  public String getId() {
    return getClass().getName();
  }

  @Nullable
  @Override
  public Runnable enableSearch(String option) {
    return null;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "The R Interpreter";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return myMainPanel;
  }

  @Override
  public boolean isModified() {
    final TheRInterpreterService interpreterService = TheRInterpreterService.getInstance();
    return !interpreterService.getInterpreterPath().equals(myInterpreterField.getText()) ||
           !interpreterService.getSourcesPath().equals(mySourcesField.getText());
  }

  @Override
  public void apply() throws ConfigurationException {
    final TheRInterpreterService interpreterService = TheRInterpreterService.getInstance();
    final String interpreterPath = myInterpreterField.getText();
    if (!StringUtil.isEmptyOrSpaces(interpreterPath))
      interpreterService.setInterpreterPath(interpreterPath);
    final String sourcesPath = mySourcesField.getText();
    if (!StringUtil.isEmptyOrSpaces(sourcesPath))
      interpreterService.setSourcesPath(sourcesPath);
  }

  @Override
  public void reset() {
    final TheRInterpreterService interpreterService = TheRInterpreterService.getInstance();
    final String interpreterPath = interpreterService.getInterpreterPath();
    myInterpreterField.setText(interpreterPath != null ? interpreterPath : "");
    final String sourcesPath = interpreterService.getSourcesPath();
    mySourcesField.setText(sourcesPath != null ? sourcesPath : "");
  }

  @Override
  public void disposeUIResources() {
  }
}
