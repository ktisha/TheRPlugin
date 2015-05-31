package com.jetbrains.ther.packages.ui;

import com.intellij.openapi.project.Project;
import com.intellij.webcore.packaging.ManagePackagesDialog;
import com.intellij.webcore.packaging.PackageManagementService;
import com.jetbrains.ther.packages.TheRPackageManagementService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TheRManagePackagesDialog extends ManagePackagesDialog {
  public TheRManagePackagesDialog(final Project project,
                                  final PackageManagementService packageManagementService,
                                  PackageManagementService.Listener packageListener) {
    super(project, packageManagementService, packageListener);
    JComponent panel = createCenterPanel();
    replaceListeners(panel != null ? panel.getComponents() : new Component[0], project, packageManagementService);
  }

  private void replaceListeners(Component[] components, final Project project, final PackageManagementService packageManagementService) {
    for (Component component : components) {
      if (component instanceof JButton) {
        JButton button = (JButton)component;
        if (button.getText().contains("Manage Repositories") && button.isVisible()) {
          for (ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
          }
          button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              TheRManageRepoDialog dialog = new TheRManageRepoDialog(project, (TheRPackageManagementService)packageManagementService);
              dialog.show();
            }
          });
        }
      }
      else {
        if (component instanceof JPanel) {
          replaceListeners(((JPanel)component).getComponents(), project, packageManagementService);
        }
      }
    }
  }
}
