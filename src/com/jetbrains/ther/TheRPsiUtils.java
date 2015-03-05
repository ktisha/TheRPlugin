package com.jetbrains.ther;

import com.intellij.psi.PsiElement;
import com.jetbrains.ther.psi.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheRPsiUtils {
  public static List<TheRExpression> getParametersExpressions(List<TheRParameter> parameters) {
    List<TheRExpression> parametersExpressions = new ArrayList<TheRExpression>();
    for (TheRParameter parameter: parameters) {
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
    PsiElement parameterList = parameter.getParent();
    if (parameterList == null || !(parameterList instanceof TheRParameterList)) {
      return null;
    }
    PsiElement functionExpression = parameterList.getParent();
    if (functionExpression == null || !(functionExpression instanceof TheRFunctionExpression)) {
      return null;
    }
    return (TheRFunctionExpression) functionExpression;
  }
}
