package com.jetbrains.ther.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.util.DocumentUtil;
import com.intellij.util.FileContentUtil;
import com.jetbrains.ther.TheRHelp;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.TheRUtils;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.interpreter.TheRSkeletonGenerator;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.psi.references.TheRStaticAnalyzerHelper;
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
    //TODO: DELETE THIS CHECK!!! it is here only for speeding checks while developing
    if (!packageName.equals("base")) {
      return;
    }
    VirtualFile skeletonsDir = packageDir.getParent();
    try {
      //TODO: move files from userSkeletons
      VirtualFile packageFile = skeletonsDir.findOrCreateChildData(this, packageName + ".r");
      final Document packageDocument = FileDocumentManager.getInstance().getDocument(packageFile);
      assert packageDocument != null;
      DocumentUtil.writeInRunUndoTransparentAction(new Runnable() {
        @Override
        public void run() {
          packageDocument.deleteString(0, packageDocument.getTextLength());
        }
      });
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
    psiFile.acceptChildren(new FunctionVisitor(packageDocument, file, project));
  }

  class FunctionVisitor extends TheRVisitor {
    private final Document myPackageDocument;
    private final VirtualFile myFile;
    private Project myProject;


    public FunctionVisitor(Document packageDocument, VirtualFile file, Project project) {
      myPackageDocument = packageDocument;
      myFile = file;
      myProject = project;
    }

    @Override
    public void visitAssignmentStatement(@NotNull final TheRAssignmentStatement o) {
      TheRPsiElement assignedValue = o.getAssignedValue();
      PsiElement assignee = o.getAssignee();

      //TODO: remember why I wrote this
      if (assignee == null) {
        PsiElement[] children = o.getChildren();
        if (children.length == 0) {
          return;
        }
        assignee = children[0];
      }
      //TODO: check if we have user skeleton for this function
      if (assignedValue instanceof TheRFunctionExpression) {
        String helpText = TheRPsiUtils.getHelpForFunction(assignee);
        if (helpText != null) {
          TheRHelp help;
          help = new TheRHelp(helpText);
          Map<TheRParameter, TheRType> parsedTypes = TheRSkeletonGeneratorHelper.guessArgsTypeFromHelp(help,
                                                                                                       (TheRFunctionExpression)assignedValue);
          if (parsedTypes != null && !parsedTypes.isEmpty()) {
            String tempFileName = myFile.getNameWithoutExtension() + "temp.r";
            try {
              VirtualFile tempFile = myFile.getParent().findOrCreateChildData(this, tempFileName);
              final Document tempDocument = FileDocumentManager.getInstance().getDocument(tempFile);
              if (tempDocument != null) {
                List<String> annotations = new ArrayList<String>();
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

                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                  @Override
                  public void run() {
                    FileDocumentManager.getInstance().saveDocument(tempDocument);
                  }
                });
                Visitor visitor = new Visitor();
                FileContentUtil.reparseFiles(tempFile);
                PsiFile psiFile = PsiManager.getInstance(myProject).findFile(tempFile);
                if (psiFile != null) {
                  psiFile.acceptChildren(visitor);
                }
                if (!visitor.hasErrors()) {
                  TheRUtils.appendToDocument(myPackageDocument, "\n\n");
                  for (String typeAnnotation : annotations) {
                    TheRUtils.appendToDocument(myPackageDocument, typeAnnotation);
                  }
                }

                tempFile.delete(this);

                //getting value type

                //TODO: fix this
                //TheRType type = TheRTypeProvider.guessReturnValueTypeFromBody((TheRFunctionExpression)assignedValue);
                //getType from function body

                TheRType type = TheRType.UNKNOWN;
                if (type != TheRType.UNKNOWN) {
                  TheRUtils.appendToDocument(myPackageDocument, "## @return " + type.toString() + "\n");
                } else {
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
      }
    }

    private void insertTypeFromHelp(PsiElement assignee, TheRHelp help) throws IOException {
      // TODO: REFACTOR IT!! MOVE TO SEPARATE FUNCTION FOR READABILITY
      TheRType valueType = TheRSkeletonGeneratorHelper.guessReturnValueTypeFromHelp(help);

      if (valueType != TheRType.UNKNOWN) {
        String valueTempFileName = myFile.getNameWithoutExtension() + "-value-temp.r";
        VirtualFile valueTempFile = myFile.getParent().findOrCreateChildData(this, valueTempFileName);
        final Document valueTempDocument = FileDocumentManager.getInstance().getDocument(valueTempFile);
        if (valueTempDocument != null && help.myExamples != null) {
          TheRUtils.appendToDocument(valueTempDocument, help.myExamples);
          TheRUtils.saveDocument(valueTempDocument);
          FileContentUtil.reparseFiles(valueTempFile);
          ValueVisitor valueVisitor = new ValueVisitor(valueType, valueTempFile, assignee.getText());
          PsiFile valuePsiFile = PsiManager.getInstance(myProject).findFile(valueTempFile);
          if (valuePsiFile != null) {
            valuePsiFile.acceptChildren(valueVisitor);
            if (valueVisitor.isOk()) {
              TheRUtils.appendToDocument(myPackageDocument, "## @return " + valueType.toString() + "\n");
            }
          }
        }
      }
    }

    class ValueVisitor extends TheRVisitor {
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
        String programString = "source(\"" + myExamplesFile + "\");"
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

  class Visitor extends  TheRVisitor {
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
