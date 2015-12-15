package com.jetbrains.ther.packages.ui;


import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.*;
import com.jetbrains.ther.packages.TheRDefaultRepository;
import com.jetbrains.ther.packages.TheRPackageManagementService;
import com.jetbrains.ther.packages.TheRPackageService;
import com.jetbrains.ther.packages.TheRRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class TheRManageRepoDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private CheckBoxList myList;
  private int currentCRANMirror;
  private TheRPackageManagementService myController;

  public TheRManageRepoDialog(@Nullable final Project project, @NotNull final TheRPackageManagementService controller) {
    super(project, false);

    setTitle("Manage Repositories");
    myMainPanel = new JPanel();
    myList = new CheckBoxList();
    final JPanel repositoryList = createRepositoriesList();
    myMainPanel.add(repositoryList);

    myController = controller;
    reloadList();

    init();
  }

  private void reloadList() {
    myList.clear();
    final List<TheRDefaultRepository> repositories = myController.getDefaultRepositories();
    TheRPackageService service = TheRPackageService.getInstance();
    for (TheRDefaultRepository repository : repositories) {
      myList.addItem(repository, repository.getUrl(), service.enabledRepositories.contains(repository.getUrl()));
    }
    for (String repository : service.userRepositories) {
      myList.addItem(repository, repository, true);
    }
  }

  private JPanel createRepositoriesList() {
    return ToolbarDecorator.createDecorator(myList)
        .disableUpDownActions()
        .setAddAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            String url = Messages.showInputDialog("Please input repository URL", "Repository URL", null);
            myList.addItem(new TheRRepository(url), url, true);
          }
        })
        .setEditAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            final int index = myList.getSelectedIndex();
            final TheRRepository oldValue = (TheRRepository)myList.getItemAt(index);
            if (oldValue != null && oldValue.getUrl().equals("@CRAN@")) {
              List<String> mirrorsList = myController.getMirrors();
              String[] mirrors = mirrorsList.toArray(new String[mirrorsList.size()]);
              currentCRANMirror = Messages.showChooseDialog("", "Choose CRAN mirror", mirrors,
                                                            mirrors[myController.getCRANMirror()], null);
            }
            else {
              String url =
                Messages.showInputDialog("Please edit repository URL", "Repository URL", null, oldValue.getUrl(), new InputValidator() {
                  @Override
                  public boolean checkInput(String inputString) {
                    return !StringUtil.isEmptyOrSpaces(inputString);
                  }

                  @Override
                  public boolean canClose(String inputString) {
                    return true;
                  }
                });
              if (!StringUtil.isEmptyOrSpaces(url) && !oldValue.getUrl().equals(url)) {
                myList.updateItem(oldValue, new TheRRepository(url), url);
              }
            }
          }
        })
        .setRemoveAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            TheRPackageService service = TheRPackageService.getInstance();
            final int index = myList.getSelectedIndex();
            final String selected = (String)myList.getItemAt(index);
            if (selected != null && service.userRepositories.contains(selected)) {
              service.userRepositories.remove(selected);
            }
            reloadList();
          }
        })
        .setRemoveActionUpdater(new AnActionButtonUpdater() {
          @Override
          public boolean isEnabled(AnActionEvent event) {
            final int index = myList.getSelectedIndex();
            return !(myList.getItemAt(index) instanceof TheRDefaultRepository);
          }
        })
        .createPanel();
  }

  @Override
  protected void doOKAction() {
    this.processDoNotAskOnOk(0);
    if (this.getOKAction().isEnabled()) {
      List<TheRRepository> enabled = Lists.newArrayList();
      for (int i = 0; i < myList.getItemsCount(); i++) {
        if (myList.isItemSelected(i)) {
          final Object item = myList.getItemAt(i);
          enabled.add((TheRRepository)item);
        }
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
