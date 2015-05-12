package com.jetbrains.ther.packages;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.InstalledPackagesPanel;
import com.intellij.webcore.packaging.PackagesNotificationPanel;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import org.jetbrains.annotations.NotNull;

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
  protected boolean canInstallPackage(@NotNull final InstalledPackage aPackage) {
    return hasInterpreterPath();
  }

  @Override
  protected boolean canUpgradePackage(InstalledPackage aPackage) {
    return hasInterpreterPath() && !TheRPackagesUtil.isPackageBase(aPackage);
  }
}
