package com.jetbrains.ther;

import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.psi.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TheRPsiUtils {
  private static final Logger LOG = Logger.getInstance(TheRPsiUtils.class);
  private static final int MINUTE = 60 * 1000;

  public static List<TheRExpression> getParametersExpressions(List<TheRParameter> parameters) {
    List<TheRExpression> parametersExpressions = new ArrayList<TheRExpression>();
    for (TheRParameter parameter : parameters) {
      parametersExpressions.add(parameter.getExpression());
    }
    return parametersExpressions;
  }

  @Nullable
  public static TheRAssignmentStatement getAssignmentStatement(@NotNull final TheRParameter parameter) {
    TheRFunctionExpression functionExpression = getFunction(parameter);
    if (functionExpression == null) {
      return null;
    }
    PsiElement assignmentStatement = functionExpression.getParent();
    if (assignmentStatement == null || !(assignmentStatement instanceof TheRAssignmentStatement)) {
      return null;
    }
    return (TheRAssignmentStatement)assignmentStatement;
  }

  @Nullable
  public static TheRFunctionExpression getFunction(TheRParameter parameter) {
    //TODO: check some conditions when we should stop
    return PsiTreeUtil.getParentOfType(parameter, TheRFunctionExpression.class);
  }

  public static boolean isNamedArgument(TheRReferenceExpression element) {
    PsiElement parent = element.getParent();
    if (parent == null || !(parent instanceof TheRAssignmentStatement)) {
      return false;
    }
    PsiElement argumentList = parent.getParent();
    return argumentList != null && argumentList instanceof TheRArgumentList;
  }

  @Nullable
  public static TheRFunctionExpression getFunction(@NotNull final TheRCallExpression callExpression) {
    TheRExpression expression = callExpression.getExpression();
    if (expression instanceof TheRReferenceExpression) {
      PsiReference reference = expression.getReference();
      if (reference == null) {
        return null;
      }
      PsiElement functionDef = reference.resolve();
      if (functionDef == null) {
        return null;
      }
      if (functionDef instanceof TheRAssignmentStatement) {
        return PsiTreeUtil.getChildOfType(functionDef, TheRFunctionExpression.class);
      }
      PsiElement assignmentStatement = functionDef.getParent();
      return PsiTreeUtil.getChildOfType(assignmentStatement, TheRFunctionExpression.class);
    }
    return null;
  }

  public static boolean containsTripleDot(List<TheRParameter> formalArguments) {
    for (TheRParameter parameter : formalArguments) {
      if (parameter.getText().equals("...")) {
        return true;
      }
    }
    return false;
  }

  public static TheRAssignmentStatement getAssignmentStatement(@NotNull final TheRFunctionExpression expression) {
    PsiElement assignmentStatement = expression.getParent();
    if (assignmentStatement != null && assignmentStatement instanceof TheRAssignmentStatement) {
      return (TheRAssignmentStatement)assignmentStatement;
    }
    return null;
  }

  public static String getHelpForFunction(PsiElement assignee, String packageName) {
    File file = TheRHelpersLocator.getHelperFile("r-help.r");
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    String helperPath = file.getAbsolutePath();
    final Process process;
    try {
      String assigneeText =
        assignee.getText().replaceAll("\"", "");
      process = Runtime.getRuntime().exec(path + " --slave -f " + helperPath + " --args " + packageName + " " + assigneeText);
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

  /**
   * No package loading. Use only for documentation.
   */
  @Nullable
  public static String getHelpForFunction(@NotNull PsiElement assignee) {
    File file = TheRHelpersLocator.getHelperFile("r-help-without-package.r");
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
  @NotNull
  public static <T extends TheRPsiElement> T[] getAllChildrenOfType(@NotNull PsiElement element, @NotNull Class<T> aClass) {
    List<T> result = new SmartList<T>();
    for (PsiElement child : element.getChildren()) {
      if (aClass.isInstance(child)) {
        //noinspection unchecked
        result.add((T)child);
      }
      else {
        ContainerUtil.addAll(result, getAllChildrenOfType(child, aClass));
      }
    }
    return ArrayUtil.toObjectArray(result, aClass);
  }

  public static boolean isReturn(TheRCallExpression expression) {
    return expression.getText().startsWith("return");
  }
}
