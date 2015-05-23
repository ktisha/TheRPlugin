package com.jetbrains.ther.packages;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.webcore.packaging.PackagesNotificationPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author avesloguzova
 */
public class TheRPackagesToolWindowFactory implements ToolWindowFactory {

  public TheRPackagesToolWindowFactory() {


  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

    PackagesNotificationPanel notificationPanel = new PackagesNotificationPanel();
    TheRInstalledPackagesPanel packagesPanel = new TheRInstalledPackagesPanel(project, notificationPanel);
    packagesPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
    packagesPanel.updatePackages(new TheRPackageManagementService(project));
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(packagesPanel, "", false);
    toolWindow.getContentManager().addContent(content);
  }
}
