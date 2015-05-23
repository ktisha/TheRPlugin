package com.jetbrains.ther.packages;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.RepoPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author avesloguzova
 */
public class TheRPackageTaskManager {
  private final Project myProject;
  private final TaskListener myListener;

  TheRPackageTaskManager(@Nullable Project project, @NotNull TaskListener listener) {
    myProject = project;
    myListener = listener;
  }

  public void install(RepoPackage pkg) {
    ProgressManager.getInstance().run(new InstallTask(myProject, myListener, pkg));
  }

  public void uninstall(List<InstalledPackage> installedPackages) {
    ProgressManager.getInstance().run(new UninstallTask(myProject, myListener, installedPackages));
  }

  public interface TaskListener {
    void started();

    void finished(List<ExecutionException> exceptions);
  }

  public abstract static class PackagingTask extends Task.Backgroundable {


    private TaskListener myListener;

    PackagingTask(@Nullable Project project, @NotNull String title, @NotNull TaskListener listener) {
      super(project, title);
      myListener = listener;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
      taskStarted(indicator);
      taskFinished(runTask(indicator));
    }

    protected void taskStarted(@NotNull ProgressIndicator indicator) {

      indicator.setText(getTitle() + "...");
      if (myListener != null) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            myListener.started();
          }
        });
      }
    }

    protected void taskFinished(@NotNull final List<ExecutionException> exceptions) {

      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          if (myListener != null) {
            myListener.finished(exceptions);
          }
        }
      });
    }

    @NotNull
    protected abstract List<ExecutionException> runTask(@NotNull ProgressIndicator indicator);

    @NotNull
    protected abstract String getSuccessTitle();

    @NotNull
    protected abstract String getSuccessDescription();

    @NotNull
    protected abstract String getFailureTitle();
  }

  public static class InstallTask extends PackagingTask {
    RepoPackage myPackage;

    InstallTask(@Nullable Project project,
                @NotNull TaskListener listener,
                @NotNull RepoPackage repoPackage) {
      super(project, "Install packages", listener);//TODO add title
      myPackage = repoPackage;
    }

    @NotNull
    @Override
    protected List<ExecutionException> runTask(@NotNull ProgressIndicator indicator) {
      final List<ExecutionException> exceptions = new ArrayList<ExecutionException>();

      try {
        TheRPackagesUtil.installPackage(myPackage);
      }
      catch (ExecutionException e) {
        exceptions.add(e);
      }
      return exceptions;
    }

    @NotNull
    @Override
    protected String getSuccessTitle() {
      return "Package installed successfully";
    }

    @NotNull
    @Override
    protected String getSuccessDescription() {
      return "Installed package " + myPackage.getName();
    }

    @NotNull
    @Override
    protected String getFailureTitle() {
      return "Install package failed";
    }
  }

  public static class UninstallTask extends PackagingTask {
    private List<InstalledPackage> myPackages;

    UninstallTask(@Nullable Project project,
                  @NotNull TaskListener listener,
                  @NotNull List<InstalledPackage> packages) {
      super(project, "Uninstall packages", listener);
      myPackages = packages;
    }

    @NotNull
    @Override
    protected List<ExecutionException> runTask(@NotNull ProgressIndicator indicator) {
      final List<ExecutionException> exceptions = new ArrayList<ExecutionException>();

      try {
        TheRPackagesUtil.uninstallPackage(myPackages);
      }
      catch (ExecutionException e) {
        exceptions.add(e);
      }
      return exceptions;
    }

    @NotNull
    @Override
    protected String getSuccessTitle() {
      return "Packages uninstalled successfully";
    }

    @NotNull
    @Override
    protected String getSuccessDescription() {
      final String packagesString = StringUtil.join(myPackages, new Function<InstalledPackage, String>() {
        @Override
        public String fun(InstalledPackage pkg) {
          return "'" + pkg.getName() + "'";
        }
      }, ", ");
      return "Uninstalled packages: " + packagesString;
    }

    @NotNull
    @Override
    protected String getFailureTitle() {
      return "Uninstall packages failed";
    }
  }
}
