package com.jetbrains.ther.packages;

import com.intellij.util.CatchingConsumer;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.PackageManagementService;
import com.intellij.webcore.packaging.RepoPackage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author avesloguzova
 */
public class TheRPackageManagementService extends PackageManagementService {

  private static TheRPackageManagementService instance;

  public static TheRPackageManagementService getInstance() {
    if (instance == null) {
      instance = new TheRPackageManagementService();
    }
    return instance;
  }

  @Override
  public boolean canModifyRepository(String repositoryUrl) {
    return false;
  }

  @Override
  public List<RepoPackage> getAllPackages() throws IOException {
    return TheRPackagesUtil.getOrLoadPackages();
  }

  @Override
  public List<RepoPackage> reloadAllPackages() throws IOException {
    TheRPackagesUtil.getAvailablePackages();
    return getAllPackages();
  }

  @Override
  public Collection<InstalledPackage> getInstalledPackages() throws IOException {
    return TheRPackagesUtil.getInstalledPackages();
  }

  @Override
  public void installPackage(final RepoPackage repoPackage, String version, boolean forceUpgrade, String extraOptions,
                             final Listener listener, boolean installToUser) {
    try {
      listener.operationStarted(String.format("Trying install package %s with all packages packages which these packages depend on/link" +
                                              " to/import/suggest (and so on recursively).", repoPackage.getName()));
      TheRPackagesUtil.installPackage(repoPackage);
      listener.operationFinished(repoPackage.getName(), null);
    }
    catch (IOException e) {
      listener.operationFinished("I/O error", new ErrorDescription("Some I/O error is occurred", null, null, null));
    }
    catch (TheRPackageManagementException e) {
      listener.operationFinished("Installation error", e.getErrorDescription());
    }
  }

  @Override
  public boolean canInstallToUser() {
    return false;
  }

  @Override
  public void uninstallPackages(List<InstalledPackage> list, Listener listener) {
    try {
      listener.operationStarted("Try to remove packages");
      TheRPackagesUtil.uninstallPackage(list);
      listener.operationFinished("Packages removed", null);
    }
    catch (IOException e) {
      listener.operationFinished("I/O error", new ErrorDescription("Some I/O error is occurred", null, null, null));
    }
    catch (TheRPackageManagementException e) {
      listener.operationFinished("Error while remove package", e.getErrorDescription());
    }
  }

  @Override
  public void fetchPackageVersions(String s, CatchingConsumer<List<String>, Exception> consumer) {

  }

  @Override
  public void fetchPackageDetails(String s, CatchingConsumer<String, Exception> consumer) {


  }
}
