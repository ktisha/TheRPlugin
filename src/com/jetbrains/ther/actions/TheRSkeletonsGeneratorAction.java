package com.jetbrains.ther.actions;

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
import com.intellij.psi.PsiReference;
import com.intellij.util.DocumentUtil;
import com.intellij.util.FileContentUtil;
import com.jetbrains.ther.TheRHelp;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.TheRUtils;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.interpreter.TheRSkeletonGenerator;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.typing.DocStringUtil;
import com.jetbrains.ther.typing.MatchingException;
import com.jetbrains.ther.typing.TheRSkeletonGeneratorHelper;
import com.jetbrains.ther.typing.TheRTypeChecker;
import com.jetbrains.ther.typing.types.TheRType;
import com.jetbrains.ther.typing.types.TheRUnionType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;


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
          Map<TheRParameter, TheRType> parsedTypes = guessArgsTypeFromHelp(help, (TheRFunctionExpression)assignedValue);
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
                //TODO: fill temp doc and check types

                tempFile.delete(this);
              }
            }
            catch (IOException e) {
              LOG.error(e);
            }
          }
        }


        TheRUtils.appendToDocument(myPackageDocument, o.getText() + "\n\n");
        TheRUtils.saveDocument(myPackageDocument);
      }
    }

    //TODO: return map
    private Map<TheRParameter, TheRType> guessArgsTypeFromHelp(TheRHelp help, TheRFunctionExpression function) {
      List<TheRParameter> parameters = function.getParameterList().getParameterList();
      Map<TheRParameter, TheRType> parsedTypes = new HashMap<TheRParameter, TheRType>();
      String argumentsDescription = help.myArguments;
      if (argumentsDescription != null && !parameters.isEmpty()) {
        parseArgumentsDescription(argumentsDescription, parameters, parsedTypes);
      }
      return parsedTypes;
    }

    private void parseArgumentsDescription(String description, List<TheRParameter> parameters, Map<TheRParameter, TheRType> parsedTypes) {
      Map<TheRParameter, String> argsDesc = new HashMap<TheRParameter, String>();
      String[] argTexts = description.split("\n\n");
      for (String argText : argTexts) {
        String[] split = argText.split(":", 2);
        if (split.length < 2) {
          continue;
        }
        String arguments = split[0];
        String[] argNames = arguments.split(",");
        for (String argName : argNames) {
          String name = argName.trim();
          TheRParameter parameter = findParameter(name, parameters);
          if (parameter != null) {
            argsDesc.put(parameter, split[1]);
          }
        }
      }
      for (Map.Entry<TheRParameter, String> entry : argsDesc.entrySet()) {
        TheRParameter parameter = entry.getKey();
        String text = entry.getValue();
        TheRType type = findType(text);
        if (type != TheRType.UNKNOWN) {
          parsedTypes.put(parameter, type);
        }
      }
    }

    private TheRType findType(String text) {
      Set<TheRType> foundTypes = new HashSet<TheRType>();
      String[] words = text.split("[^a-zA-Z/-]");
      for (String word : words) {
        if (word.isEmpty()) {
          continue;
        }
        TheRType type = TheRSkeletonGeneratorHelper.TYPES.get(word);
        if (type != null) {
          foundTypes.add(type);
        }
      }
      return TheRUnionType.create(foundTypes);
    }

    private TheRParameter findParameter(String name, List<TheRParameter> parameters) {
      for (TheRParameter parameter : parameters) {
        if (name.equals(parameter.getName())) {
          return parameter;
        }
      }
      return null;
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
