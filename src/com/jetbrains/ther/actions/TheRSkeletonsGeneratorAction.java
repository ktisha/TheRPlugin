package com.jetbrains.ther.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.jetbrains.ther.interpreter.TheRSkeletonGenerator;
import org.jetbrains.annotations.NotNull;


public class TheRSkeletonsGeneratorAction extends AnAction {

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getProject();
    final Application application = ApplicationManager.getApplication();

    application.invokeLater(new Runnable() {
      @Override
      public void run() {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Updating skeletons", false) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            TheRSkeletonGenerator.runSkeletonGeneration();
          }
        });
      }
    });
  }
}
