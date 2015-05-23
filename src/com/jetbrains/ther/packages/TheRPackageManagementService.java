package com.jetbrains.ther.packages;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.project.Project;
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

  private final Project myProject;

  public TheRPackageManagementService(Project project) {
    myProject = project;
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

    final TheRPackageTaskManager manager = new TheRPackageTaskManager(myProject, new TheRPackageTaskManager.TaskListener() {
      @Override
      public void started() {
        listener.operationStarted(repoPackage.getName());
      }

      @Override
      public void finished(List<ExecutionException> exceptions) {
        listener.operationFinished(repoPackage.getName(), toErrorDescription(exceptions));
      }
    });
    manager.install(repoPackage);
  }

  private ErrorDescription toErrorDescription(List<ExecutionException> exceptions) {
    //noinspection LoopStatementThatDoesntLoop
    for (ExecutionException e : exceptions) {
      if (e instanceof TheRExecutionException) {
        TheRExecutionException exception = (TheRExecutionException)e;
        return new ErrorDescription(exception.getMessage(), exception.getCommand(), exception.getStderr(), null);
      }
      else {
        return new ErrorDescription(e.getMessage(), null, null, null);
      }
    }
    return null;
  }

  @Override
  public boolean canInstallToUser() {
    return false;
  }

  @Override
  public void uninstallPackages(List<InstalledPackage> installedPackages, final Listener listener) {
    //try {
    //  listener.operationStarted("Try to remove packages");
    //  TheRPackagesUtil.uninstallPackage(list);
    //  listener.operationFinished("Packages removed", null);
    //}
    //catch (IOException e) {
    //  listener.operationFinished("I/O error", new ErrorDescription("Some I/O error is occurred", null, null, null));
    //}
    //catch (TheRPackageManagementException e) {
    //  listener.operationFinished("Error while remove package", e.getErrorDescription());
    //}
    final String packageName = installedPackages.size() == 1 ? installedPackages.get(0).getName() : null;
    final TheRPackageTaskManager manager = new TheRPackageTaskManager(myProject, new TheRPackageTaskManager.TaskListener() {
      @Override
      public void started() {
        listener.operationStarted(packageName);
      }

      @Override
      public void finished(List<ExecutionException> exceptions) {
        listener.operationFinished(packageName, toErrorDescription(exceptions));
      }
    });
    manager.uninstall(installedPackages);
  }

  @Override
  public void fetchPackageVersions(String s, CatchingConsumer<List<String>, Exception> consumer) {

  }

  @Override
  public void fetchPackageDetails(String s, CatchingConsumer<String, Exception> consumer) {


  }
}
