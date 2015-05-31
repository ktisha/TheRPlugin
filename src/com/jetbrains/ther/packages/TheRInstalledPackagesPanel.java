package com.jetbrains.ther.packages;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.webcore.packaging.*;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.packages.ui.TheRManagePackagesDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author avesloguzova
 */
public class TheRInstalledPackagesPanel extends InstalledPackagesPanel {
  public TheRInstalledPackagesPanel(Project project,
                                    PackagesNotificationPanel area) {
    super(project, area);
  }

  public static boolean hasInterpreterPath() {
    return StringUtil.isNotEmpty(TheRInterpreterService.getInstance().getInterpreterPath());
  }

  @Override
  protected boolean canUninstallPackage(InstalledPackage aPackage) {
    return hasInterpreterPath() && !TheRPackagesUtil.isPackageBase(aPackage);
  }

  @Override
  @NotNull
  protected ManagePackagesDialog createManagePackagesDialog() {
    return new TheRManagePackagesDialog(this.myProject, this.myPackageManagementService, new PackageManagementService.Listener() {
      public void operationStarted(String packageName) {
        myPackagesTable.setPaintBusy(true);
      }

      public void operationFinished(String packageName, @Nullable PackageManagementService.ErrorDescription errorDescription) {
        myNotificationArea.showResult(packageName, errorDescription);
        myPackagesTable.clearSelection();
        doUpdatePackages(myPackageManagementService);
      }
    });
  }

  @Override
  protected boolean canInstallPackage(@NotNull final InstalledPackage aPackage) {
    return hasInterpreterPath();
  }

  @Override
  protected boolean canUpgradePackage(InstalledPackage aPackage) {
    return hasInterpreterPath() && !TheRPackagesUtil.isPackageBase(aPackage);
  }
}
