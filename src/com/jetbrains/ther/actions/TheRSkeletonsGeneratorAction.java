package com.jetbrains.ther.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.DocumentUtil;
import com.intellij.util.FileContentUtil;
import com.jetbrains.ther.TheRHelp;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.TheRUtils;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.interpreter.TheRSkeletonGenerator;
import com.jetbrains.ther.psi.TheRRecursiveElementVisitor;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.TheRStaticAnalyzerHelper;
import com.jetbrains.ther.typing.*;
import com.jetbrains.ther.typing.types.TheRType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TheRSkeletonsGeneratorAction extends AnAction {

  private static final Logger LOG = Logger.getInstance(TheRSkeletonsGeneratorAction.class);

  public static void generateSmartSkeletons(@NotNull final Project project) {
    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Updating skeletons", false) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            TheRSkeletonGenerator.runSkeletonGeneration();
            PsiDocumentManager.getInstance(project).commitAllDocuments();
            final String path = TheRSkeletonGenerator.getSkeletonsPath(TheRInterpreterService.getInstance().getInterpreterPath());
            VirtualFile skeletonDir = VfsUtil.findFileByIoFile(new File(path), true);
            if (skeletonDir == null) {
              LOG.info("Failed to locate skeletons directory");
              return;
            }
            for (final VirtualFile packageDir : skeletonDir.getChildren()) {
              if (!packageDir.isDirectory()) {
                continue;
              }
              ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                  ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                      generateSkeletonsForPackage(packageDir, project);
                      try {
                        packageDir.delete(this);
                      }
                      catch (IOException e) {
                        LOG.error("Failed to delete " + packageDir.getPath());
                      }
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

  public void actionPerformed(AnActionEvent event) {
    final Project project = event.getProject();
    assert project != null;
    TheRSkeletonsGeneratorAction.generateSmartSkeletons(project);
  }

  @Override
  public void update(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    if (!ApplicationManager.getApplication().isInternal()) {
      presentation.setEnabled(false);
      presentation.setVisible(false);
      return;
    }
    presentation.setVisible(true);
    presentation.setEnabled(true);
  }

  private static void generateSkeletonsForPackage(@NotNull final VirtualFile packageDir, @NotNull final Project project) {
    String packageName = packageDir.getName();
    //TODO: DELETE THIS CHECK!!! it is here only for speeding checks while developing
    if (!packageName.equals("base") && !packageName.equals("codetools")) {
      return;
    }
    VirtualFile skeletonsDir = packageDir.getParent();
    try {
      VirtualFile packageFile = skeletonsDir.findOrCreateChildData(project, packageName + ".r");
      final Document packageDocument = FileDocumentManager.getInstance().getDocument(packageFile);
      assert packageDocument != null;
      DocumentUtil.writeInRunUndoTransparentAction(new Runnable() {
        @Override
        public void run() {
          packageDocument.deleteString(0, packageDocument.getTextLength());
        }
      });
      for (final VirtualFile file : packageDir.getChildren()) {
        generateSkeletonsForFile(file, packageDocument, project, packageName);
      }
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }

  private static void generateSkeletonsForFile(@NotNull final VirtualFile file,
                                               @NotNull final Document packageDocument,
                                               @NotNull final Project project,
                                               String packageName) {
    LOG.info("start processing " + file.getPath());
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    assert psiFile != null;
    psiFile.acceptChildren(new FunctionVisitor(packageDocument, file, project, packageName));
  }

  static class FunctionVisitor extends TheRVisitor {
    private final Document myPackageDocument;
    private final VirtualFile myFile;
    private final String myPackageName;
    private Project myProject;


    public FunctionVisitor(Document packageDocument, VirtualFile file, Project project, String packageName) {
      myPackageDocument = packageDocument;
      myFile = file;
      myProject = project;
      myPackageName = packageName;
    }

    @Override
    public void visitAssignmentStatement(@NotNull final TheRAssignmentStatement o) {
      TheRPsiElement assignedValue = o.getAssignedValue();
      PsiElement assignee = o.getAssignee();

      //we need this check because of functions like "!" <- ...
      //TODO: delete this check
      if (assignee == null) {
        PsiElement[] children = o.getChildren();
        if (children.length == 0) {
          return;
        }
        assignee = children[0];
      }
      //TODO: check if we have user skeleton for this function
      if (assignedValue instanceof TheRFunctionExpression) {
        if (assignee.getText().equals("all")) {
          System.out.println();
        }
        String helpText = TheRPsiUtils.getHelpForFunction(assignee, myPackageName);
        if (helpText != null) {
          final TheRHelp help;
          help = new TheRHelp(helpText);
          final Map<TheRParameter, TheRType> parsedTypes = TheRSkeletonGeneratorHelper.guessArgsTypeFromHelp(help,
                                                                                                             (TheRFunctionExpression)assignedValue);
          if (parsedTypes != null && !parsedTypes.isEmpty()) {
            String tempFileName = myFile.getNameWithoutExtension() + "temp.r";
            try {
              VirtualFile tempFile = myFile.getParent().findOrCreateChildData(this, tempFileName);
              final Document tempDocument = FileDocumentManager.getInstance().getDocument(tempFile);
              if (tempDocument != null) {
                final List<String> annotations = new ArrayList<String>();
                for (Map.Entry<TheRParameter, TheRType> entry : parsedTypes.entrySet()) {
                  String typeAnnotation = DocStringUtil.generateTypeAnnotation(entry.getKey(), entry.getValue());
                  TheRUtils.appendToDocument(tempDocument, typeAnnotation);
                  annotations.add(typeAnnotation);
                }
                TheRUtils.appendToDocument(tempDocument, o.getText() + "\n");

                if (help.myExamples != null && !help.myExamples.isEmpty()) {
                  TheRUtils.appendToDocument(tempDocument, "\n" + help.myExamples + "\n");
                }

                if (help.myUsage != null && !help.myUsage.isEmpty()) {
                  TheRUtils.appendToDocument(tempDocument, "\n" + help.myUsage + "\n");
                }
                TheRUtils.saveDocument(tempDocument);
                PsiDocumentManager.getInstance(myProject).commitDocument(tempDocument);
                Visitor visitor = new Visitor();
                FileContentUtil.reparseFiles(tempFile);
                PsiFile psiFile = PsiManager.getInstance(myProject).findFile(tempFile);
                if (psiFile != null && psiFile.isValid()) {
                  psiFile.acceptChildren(visitor);
                }
                if (!visitor.hasErrors()) {
                  TheRUtils.appendToDocument(myPackageDocument, "\n\n");
                  for (String typeAnnotation : annotations) {
                    TheRUtils.appendToDocument(myPackageDocument, typeAnnotation);
                  }
                }

                tempFile.delete(this);


                TheRType type = TheRTypeProvider.guessReturnValueTypeFromBody((TheRFunctionExpression)assignedValue);
                if (type != TheRType.UNKNOWN) {
                  TheRUtils.appendToDocument(myPackageDocument, "## @return " + type.toString() + "\n");
                }
                else {
                  insertTypeFromHelp(assignee, help);
                }
              }
            }
            catch (IOException e) {
              LOG.error(e);
            }
          }
        }

        Set<String> unusedParameters = TheRStaticAnalyzerHelper.optionalParameters((TheRFunctionExpression)assignedValue);

        if (!unusedParameters.isEmpty()) {
          TheRUtils.appendToDocument(myPackageDocument, "## @optional " + StringUtil.join(unusedParameters, ", ") + "\n");
        }
        TheRUtils.appendToDocument(myPackageDocument, o.getText() + "\n\n");
        TheRUtils.saveDocument(myPackageDocument);
        LOG.info("end processing " + myFile.getPath());
      }
    }

    private void insertTypeFromHelp(PsiElement assignee, final TheRHelp help) throws IOException {
      TheRType valueType = TheRSkeletonGeneratorHelper.guessReturnValueTypeFromHelp(help);
      if (valueType != TheRType.UNKNOWN) {
        String valueTempFileName = myFile.getNameWithoutExtension() + "-value-temp.r";
        VirtualFile valueTempFile = myFile.getParent().findOrCreateChildData(this, valueTempFileName);
        final Document valueTempDocument = FileDocumentManager.getInstance().getDocument(valueTempFile);
        if (valueTempDocument != null && help.myExamples != null) {
          TheRUtils.appendToDocument(valueTempDocument, help.myExamples);
          TheRUtils.saveDocument(valueTempDocument);
          PsiDocumentManager.getInstance(myProject).commitDocument(valueTempDocument);
          ValueVisitor valueVisitor = new ValueVisitor(valueType, valueTempFile, assignee.getText());
          PsiFile valuePsiFile = PsiManager.getInstance(myProject).findFile(valueTempFile);
          if (valuePsiFile != null && valuePsiFile.isValid()) {
            valuePsiFile.acceptChildren(valueVisitor);
            if (valueVisitor.isOk()) {
              TheRUtils.appendToDocument(myPackageDocument, "## @return " + valueType.toString() + "\n");
            }
          }
        }
        valueTempFile.delete(this);
      }
    }

    class ValueVisitor extends TheRRecursiveElementVisitor {
      private Boolean myOk = null;
      private final TheRType myCandidate;
      private final VirtualFile myExamplesFile;
      private final String myFunctionName;

      ValueVisitor(TheRType candidate, VirtualFile examplesFile, String functionName) {
        myCandidate = candidate;
        myExamplesFile = examplesFile;
        myFunctionName = functionName;
      }

      @Override
      public void visitCallExpression(@NotNull TheRCallExpression o) {
        if (!myFunctionName.equals(o.getExpression().getName())) {
          return;
        }
        if (myOk != null && !myOk) {
          return;
        }
        String packageQuoted = "\"" + myPackageName + "\"";
        String programString = "library(package=" + packageQuoted + ", character.only=TRUE);"
                               + "loadNamespace(package=" + packageQuoted + ");"
                               + "source(\"" + myExamplesFile + "\");"
                               + "myValueType<-class(" + o.getText() + ");"
                               + "print(myValueType)";
        String rPath = TheRInterpreterService.getInstance().getInterpreterPath();
        try {
          GeneralCommandLine gcl = new GeneralCommandLine();
          gcl.withWorkDirectory(myExamplesFile.getParent().getPath());
          gcl.setExePath(rPath);
          gcl.addParameter("--slave");
          gcl.addParameter("-e");
          gcl.addParameter(programString);
          Process rProcess = gcl.createProcess();
          final CapturingProcessHandler processHandler = new CapturingProcessHandler(rProcess);
          final ProcessOutput output = processHandler.runProcess(20000);
          String stdout = output.getStdout();
          TheRType evaluatedType = TheRSkeletonGeneratorHelper.findType(stdout);
          myOk = TheRTypeChecker.matchTypes(myCandidate, evaluatedType);
        }
        catch (ExecutionException e) {
          e.printStackTrace();
        }
      }

      public boolean isOk() {
        return myOk != null && myOk;
      }
    }
  }

  static class Visitor extends TheRVisitor {
    private boolean hasErrors = false;

    @Override
    public void visitCallExpression(@NotNull TheRCallExpression callExpression) {
      PsiReference referenceToFunction = callExpression.getExpression().getReference();
      if (referenceToFunction != null) {
        PsiElement assignmentStatement = referenceToFunction.resolve();
        if (assignmentStatement != null && assignmentStatement instanceof TheRAssignmentStatement) {
          TheRAssignmentStatement assignment = (TheRAssignmentStatement)assignmentStatement;
          TheRPsiElement assignedValue = assignment.getAssignedValue();
          if (assignedValue != null && assignedValue instanceof TheRFunctionExpression) {
            TheRFunctionExpression function = (TheRFunctionExpression)assignedValue;
            List<TheRExpression> arguments = callExpression.getArgumentList().getExpressionList();
            try {
              TheRTypeChecker.checkTypes(arguments, function);
            }
            catch (MatchingException e) {
              hasErrors = true;
            }
          }
        }
      }
    }

    public boolean hasErrors() {
      return hasErrors;
    }
  }
}
