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
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.typing.TheRSkeletonGeneratorHelper;
import com.jetbrains.ther.typing.types.TheRType;
import com.jetbrains.ther.typing.types.TheRUnionType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class TheRSkeletonsGeneratorAction extends AnAction {

  private static final Logger LOG = Logger.getInstance(TheRSkeletonsGeneratorAction.class);
  private static final int MINUTE = 60 * 1000;

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
      //TODO: check if we have user skeleton for this function
      if (assignedValue instanceof TheRFunctionExpression) {
        String helpText = getHelpForFunction(assignee);
        if (helpText != null) {
          TheRHelp help = new TheRHelp(helpText);
          guessArgsTypeFromHelp(help, (TheRFunctionExpression)assignedValue);
          System.out.println();
        }
      }
    }
  }

  //TODO: return map
  private void guessArgsTypeFromHelp(TheRHelp help, TheRFunctionExpression function) {
    List<TheRParameter> parameters = function.getParameterList().getParameterList();
    String argumentsDescription = help.myArguments;
    if (argumentsDescription == null || parameters.isEmpty()) {
      //return null;
      return;
    }
    Map<TheRParameter, TheRType> parsedTypes = new HashMap<TheRParameter, TheRType>();
    parseArgumentsDescription(argumentsDescription, parameters, parsedTypes);

  }

  private void parseArgumentsDescription(String description, List<TheRParameter> parameters, Map<TheRParameter, TheRType> parsedTypes) {
    Map <TheRParameter, String> argsDesc = new HashMap<TheRParameter, String>();
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
      parsedTypes.put(parameter, findType(text));
    }
    System.out.println();
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

  private String getHelpForFunction(PsiElement assignee) {
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
      String stdout = output.getStdout();
      if (stdout.startsWith("No documentation")) {
        return null;
      }
      return stdout;
    }
    catch (IOException e) {
      LOG.error(e);
    }
    return null;
  }

  private static class TheRHelp {
    String myArguments;
    String myValue;
    String myUsage;
    String myExamples;

    public TheRHelp(String documentationText) {
      String[] lines = documentationText.split("\n");
      String section = null;
      StringBuilder sectionText = null;
      for (String line : lines) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
          if (section != null && sectionText.length() > 0) {
            sectionText.append('\n');
          }
        } else {
          if (line.startsWith("\t") || line.startsWith(" ")) {
            if (section != null) {
              sectionText.append(trimmed);
              sectionText.append('\n');
            }
          } else {
            if (trimmed.endsWith(":")) {
              saveSection(section, sectionText);
              section = trimmed.substring(0, trimmed.length() - 1);
              sectionText = new StringBuilder();
            } else {
              if (section != null) {
                sectionText.append(trimmed);
                sectionText.append('\n');
              }
            }
          }
        }
      }
      saveSection(section, sectionText);
    }

    private void saveSection(String section, StringBuilder sectionText) {
      if (section != null) {
        section = section.replaceAll("_\b", "");
        if ("Arguments".equals(section)) {
          myArguments = sectionText.toString();
        }
        else if ("Value".equals(section)) {
          myValue = sectionText.toString();
        }
        else if ("Usage".equals(section)) {
          myUsage = sectionText.toString();
        }
        else if ("Examples".equals(section)) {
          myExamples = sectionText.toString();
        }
      }
    }
  }
}
