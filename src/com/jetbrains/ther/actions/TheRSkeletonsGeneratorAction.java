package com.jetbrains.ther.actions;

import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.ther.TheRHelpersLocator;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.interpreter.TheRSkeletonGenerator;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRFunctionExpression;
import com.jetbrains.ther.psi.api.TheRPsiElement;
import com.jetbrains.ther.psi.api.TheRVisitor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;


public class TheRSkeletonsGeneratorAction extends AnAction {

  private static final Logger LOG = Logger.getInstance(TheRSkeletonsGeneratorAction.class);

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getProject();
    assert project != null;
    final Application application = ApplicationManager.getApplication();

    application.invokeLater(new Runnable() {
      @Override
      public void run() {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Updating skeletons", false) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            TheRSkeletonGenerator.runSkeletonGeneration();
            final String path = TheRSkeletonGenerator.getSkeletonsPath(TheRInterpreterService.getInstance().getInterpreterPath());
            VirtualFile skeletonDir = VfsUtil.findFileByIoFile(new File(path), true);
            if (skeletonDir == null) {
              LOG.info("Failed to locate skeletons directory");
              return;
            }
            for (final VirtualFile packageDir : skeletonDir.getChildren()) {
              ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                  ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                      generateSkeletonsForPackage(packageDir, project);
                    }
                  });
                }
              });
            }
          }
        });
      }
    });
  }

  private void generateSkeletonsForPackage(@NotNull final VirtualFile packageDir, @NotNull final Project project) {
    String packageName = packageDir.getName();
    //TODO: DELETE THIS CHECK!!!
    if (!packageName.equals("base")) {
      return;
    }
    VirtualFile skeletonsDir = packageDir.getParent();
    try {
      //TODO: move files from userSkeletons
      VirtualFile packageFile = skeletonsDir.findOrCreateChildData(this, packageName + ".r");
      Document packageDocument = FileDocumentManager.getInstance().getDocument(packageFile);
      assert packageDocument != null;
      for (final VirtualFile file : packageDir.getChildren()) {
        generateSkeletonsForFile(file, packageDocument, project);
      }
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }

  private void generateSkeletonsForFile(@NotNull final VirtualFile file,
                                        @NotNull final Document packageDocument,
                                        @NotNull final Project project) {
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    assert psiFile != null;
    psiFile.acceptChildren(new FunctionVisitor());
  }

  class FunctionVisitor extends TheRVisitor {
    private static final int MINUTE = 60 * 1000;

    @Override
    public void visitAssignmentStatement(@NotNull TheRAssignmentStatement o) {
      TheRPsiElement assignedValue = o.getAssignedValue();
      PsiElement assignee = o.getAssignee();
      if (assignee == null) {
        PsiElement[] children = o.getChildren();
        if (children.length == 0) {
          return;
        }
        assignee = children[0];
      }
      if (assignedValue instanceof TheRFunctionExpression) {
        File file = TheRHelpersLocator.getHelperFile("r-help.r");
        final String path = TheRInterpreterService.getInstance().getInterpreterPath();
        String helperPath = file.getAbsolutePath();
        final Process process;
        try {
          String assigneeText =
            assignee.getText().replaceAll("\"", "");
          process = Runtime.getRuntime().exec(path + " --slave -f " + helperPath + " --args " + assigneeText);
          final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
          final ProcessOutput output = processHandler.runProcess(MINUTE * 5);
          String documentation = output.getStdout();
        }
        catch (IOException e) {
          LOG.error(e);
        }

      }
    }
  }
}
