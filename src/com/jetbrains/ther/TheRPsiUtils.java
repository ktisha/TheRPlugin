package com.jetbrains.ther;

import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Predicate;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.psi.stubs.TheRAssignmentNameIndex;
import com.jetbrains.ther.typing.MatchingException;
import com.jetbrains.ther.typing.TheRTypeChecker;
import com.jetbrains.ther.typing.TheRTypeProvider;
import com.jetbrains.ther.typing.types.TheRFunctionType;
import com.jetbrains.ther.typing.types.TheRType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
      return getFunctionFromReference(expression.getReference());
    }
    return null;
  }

  @Nullable
  private static TheRFunctionExpression getFunctionFromReference(PsiReference reference) {
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

  @Nullable
  public static TheRFunctionExpression getFunction(@NotNull final TheROperatorExpression binaryExpression) {
    TheROperator operator = PsiTreeUtil.getChildOfType(binaryExpression, TheROperator.class);
    if (operator != null) {
      return getFunctionFromReference(operator.getReference());
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

  public static TheRCallExpression findCall(Project project, String functionName, Predicate<TheRCallExpression> predicate) {
    ProjectAndLibrariesScope scope = new ProjectAndLibrariesScope(project);
    Collection<TheRAssignmentStatement> possibleDefinitions =
      TheRAssignmentNameIndex.find(functionName, project, scope);
    TheRAssignmentStatement functionDefinition = null;
    for (TheRAssignmentStatement assignment : possibleDefinitions) {
      if (assignment.getAssignedValue() instanceof TheRFunctionExpression) {
        functionDefinition = assignment;
        break;
      }
    }
    if (functionDefinition == null) {
      return null;
    }
    for (PsiReference reference : ReferencesSearch.search(functionDefinition, scope)) {
      PsiElement referenceFrom = reference.getElement();
      PsiElement parent = referenceFrom.getParent();
      if (parent == null || !TheRCallExpression.class.isInstance(parent)) {
        continue;
      }
      TheRCallExpression call = (TheRCallExpression)parent;
      if (predicate.apply(call)) {
        return call;
      }
    }
    return null;
  }

  public static TheRExpression findParameterValue(String param, TheRCallExpression callExpression) {
    return findParameterValues(callExpression, param).get(param);
  }

  public static Map<String, TheRExpression> findParameterValues(TheRCallExpression callExpression, String... params) {
    TheRFunctionExpression function = TheRPsiUtils.getFunction(callExpression);
    final TheRFunctionType functionType;
    if (function != null) {
      functionType = new TheRFunctionType(function);
    } else {
      TheRType type = TheRTypeProvider.getType(callExpression.getExpression());
      if (!TheRFunctionType.class.isInstance(type)) {
        return Collections.emptyMap();
      }
      functionType = (TheRFunctionType)type;
    }
    Map<TheRExpression, TheRParameter> matchedParams = new HashMap<TheRExpression, TheRParameter>();
    List<TheRExpression> matchedByTripleDot = new ArrayList<TheRExpression>();
    try {
      TheRTypeChecker.matchArgs(callExpression.getArgumentList().getExpressionList(), matchedParams, matchedByTripleDot, functionType);
    }
    catch (MatchingException e) {
      return Collections.emptyMap();
    }
    Map<String, TheRExpression> result = new HashMap<String, TheRExpression>();
    for (Map.Entry<TheRExpression, TheRParameter> entry : matchedParams.entrySet()) {
      String parameterName = entry.getValue().getName();
      TheRExpression expression = entry.getKey();
      if (expression instanceof TheRAssignmentStatement) {
        expression = (TheRExpression)((TheRAssignmentStatement)expression).getAssignedValue();
      }
      for (String param : params) {
        if (param != null && param.equals(parameterName)) {
          result.put(param, expression);
        }
      }
    }
    return result;
  }
}
