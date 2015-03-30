package com.jetbrains.ther;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.ther.psi.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheRPsiUtils {
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
}
