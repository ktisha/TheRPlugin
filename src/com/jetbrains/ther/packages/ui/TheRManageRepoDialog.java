package com.jetbrains.ther.packages.ui;


import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBList;
import com.jetbrains.ther.packages.TheRDefaultRepository;
import com.jetbrains.ther.packages.TheRPackageManagementService;
import com.jetbrains.ther.packages.TheRRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class TheRManageRepoDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private JButton myButtonToEnabled;
  private JButton myButtonToDefault;
  private JButton myRemoveButton;
  private JButton myAddButton;
  private JButton myEditButton;
  private JButton myChooseCRANMirrorButton;
  private JPanel myEnabledPanel;
  private JPanel myDefaultPanel;
  private JBList myEnabledList;
  private JBList myDefaultList;
  private List<TheRDefaultRepository> myDefaultRepos;
  private int currentCRANMirror;
  private TheRPackageManagementService myController;

  public TheRManageRepoDialog(@Nullable final Project project, @NotNull final TheRPackageManagementService controller) {
    super(project, false);

    setTitle("Manage Repositories");
    myController = controller;
    final DefaultListModel defaultRepoModel = new DefaultListModel();
    final DefaultListModel enabledRepoModel = new DefaultListModel();
    myDefaultRepos = controller.getDefaultRepositories();
    for (TheRDefaultRepository repository : myDefaultRepos) {
      defaultRepoModel.addElement(repository);
    }
    List<String> allRepositories = controller.getAllRepositories();
    for (String repoUrl : allRepositories) {
      TheRDefaultRepository defaultRepository = findDefaultRepo(repoUrl);
      if (defaultRepository != null) {
        enabledRepoModel.addElement(defaultRepository);
        defaultRepoModel.removeElement(defaultRepository);
      }
      else {
        enabledRepoModel.addElement(new TheRRepository(repoUrl));
      }
    }


    myEnabledList = new JBList(enabledRepoModel);
    myDefaultList = new JBList(defaultRepoModel);

    myEnabledList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        Object o = myEnabledList.getSelectedValue();
        myDefaultList.clearSelection();
        if (o instanceof TheRDefaultRepository) {
          myButtonToEnabled.setEnabled(false);
          myButtonToDefault.setEnabled(true);
          myEditButton.setEnabled(false);
          myRemoveButton.setEnabled(false);
        }
        else {
          myButtonToEnabled.setEnabled(false);
          myButtonToDefault.setEnabled(false);
          myEditButton.setEnabled(true);
          myRemoveButton.setEnabled(true);
        }
      }
    });
    myEnabledPanel.add(myEnabledList);
    myDefaultList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        Object o = myDefaultList.getSelectedValue();
        myEnabledList.clearSelection();
        myButtonToEnabled.setEnabled(true);
        myButtonToDefault.setEnabled(false);
        myEditButton.setEnabled(false);
        myRemoveButton.setEnabled(false);
      }
    });
    myDefaultPanel.add(myDefaultList);
    initButtons(enabledRepoModel, defaultRepoModel);
    myChooseCRANMirrorButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        List<String> mirrorsList = controller.getMirrors();
        String[] mirrors = mirrorsList.toArray(new String[mirrorsList.size()]);
        currentCRANMirror = Messages.showChooseDialog(project, "", "", null, mirrors, mirrors[controller.getCRANMirror()]);
      }
    });
    init();
    setButtonsToDisable();
  }

  private TheRDefaultRepository findDefaultRepo(String URL) {
    for (TheRDefaultRepository repo : myDefaultRepos) {
      if (repo.getUrl().equals(URL)) {
        return repo;
      }
    }
    return null;
  }

  private void setButtonsToDisable() {
    myEnabledList.clearSelection();
    myDefaultList.clearSelection();
    myButtonToEnabled.setEnabled(false);
    myButtonToDefault.setEnabled(false);
    myEditButton.setEnabled(false);
    myRemoveButton.setEnabled(false);
  }

  private void initButtons(final DefaultListModel enabledRepoModel, final DefaultListModel defaultRepoModel) {

    myAddButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String url = Messages.showInputDialog("Please input repository URL", "Repository URL", null);
        TheRRepository tmp = new TheRRepository(url);
        if (!enabledRepoModel.contains(url) && !StringUtil.isEmptyOrSpaces(url)) {
          enabledRepoModel.addElement(new TheRRepository(url));
          setButtonsToDisable();
        }
      }
    });
    myRemoveButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TheRRepository selected = (TheRRepository)myEnabledList.getSelectedValue();
        enabledRepoModel.removeElement(selected);
        setButtonsToDisable();
      }
    });
    myEditButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final TheRRepository oldValue = (TheRRepository)myEnabledList.getSelectedValue();

        String url =
          Messages.showInputDialog("Please edit repository URL", "Repository URL", null, oldValue.getUrl(), new InputValidator() {
            @Override
            public boolean checkInput(String inputString) {
              return !enabledRepoModel.contains(new TheRRepository(inputString));
            }

            @Override
            public boolean canClose(String inputString) {
              return true;
            }
          });
        if (!StringUtil.isEmptyOrSpaces(url) && !oldValue.equals(url)) {
          enabledRepoModel.addElement(new TheRRepository(url));
          enabledRepoModel.removeElement(oldValue);
        }
        setButtonsToDisable();
      }
    });
    myButtonToDefault.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TheRDefaultRepository repository = (TheRDefaultRepository)myEnabledList.getSelectedValue();
        defaultRepoModel.addElement(repository);
        enabledRepoModel.removeElement(repository);
        setButtonsToDisable();
      }
    });
    myButtonToEnabled.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TheRDefaultRepository repository = (TheRDefaultRepository)myDefaultList.getSelectedValue();
        enabledRepoModel.addElement(repository);
        defaultRepoModel.removeElement(repository);
        setButtonsToDisable();
      }
    });
  }

  @Override
  protected void doOKAction() {
    this.processDoNotAskOnOk(0);
    if (this.getOKAction().isEnabled()) {
      List<TheRRepository> enabled = Lists.newArrayList();
      ListModel model = myEnabledList.getModel();
      for (int i = 0; i < model.getSize(); i++) {
        enabled.add((TheRRepository)model.getElementAt(i));
      }
      myController.setCRANMirror(currentCRANMirror);
      myController.setRepositories(enabled);
      this.close(0);
    }
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }
}
