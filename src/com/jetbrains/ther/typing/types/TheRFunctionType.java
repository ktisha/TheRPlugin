package com.jetbrains.ther.typing.types;

import com.intellij.psi.PsiManager;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.TheRStaticAnalyzerHelper;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.typing.*;

import java.util.*;

public class TheRFunctionType extends TheRType {
  private final TheRFunctionExpression myFunctionExpression;
  private TheRType myReturnType;
  private List<TheRFunctionRule> myRules = new ArrayList<TheRFunctionRule>();
  private Map<String, TheRTypedParameter> myParameters = new HashMap<String, TheRTypedParameter>();

  public TheRFunctionType(TheRFunctionExpression functionExpression) {
    myFunctionExpression = functionExpression;
    List<TheRParameter> parameters = functionExpression.getParameterList().getParameterList();
    for (TheRParameter parameter : parameters) {
      String parameterName = parameter.getName();
      myParameters.put(parameterName, new TheRTypedParameter(parameterName, null, parameter));
    }
    createFunctionType();
  }

  @Override
  public String getCanonicalName() {
    return "function";
  }

  @Override
  public TheRType resolveType(TheRTypeEnvironment env) {
    throw new UnsupportedOperationException();
  }

  private void createFunctionType() {
    TheRAssignmentStatement assignmentStatement = TheRPsiUtils.getAssignmentStatement(myFunctionExpression);
    if (assignmentStatement != null) {
      List<Substring> lines = DocStringUtil.getDocStringLines(assignmentStatement);
      for (Substring line: lines) {
        new TheRAnnotationParser(this).interpretLine(line);
      }
    }
    Set<String> strings = TheRStaticAnalyzerHelper.optionalParameters(myFunctionExpression);
    for (String name : strings) {
      TheRTypedParameter typedParameter = myParameters.get(name);
      if (typedParameter != null) {
        typedParameter.setOptional(true);
      }
    }
    for (TheRTypedParameter parameter : myParameters.values()) {
      Set<TheRType> parameterTypes = new HashSet<TheRType>();
      TheRType fromAnnotation = parameter.getType();
      if (fromAnnotation != null) {
        parameterTypes.add(fromAnnotation);
        TheRExpression defaultValue = parameter.getParameter().getExpression();
        if (defaultValue != null) {
          TheRType defaultType = TheRTypeProvider.getType(defaultValue);
          if (!TheRUnknownType.class.isInstance(defaultType)) {
            parameterTypes.add(defaultType);
          }
        }
      }
      if (!parameterTypes.isEmpty()) {
        parameter.setType(TheRUnionType.create(parameterTypes));
      }
    }
    if (myReturnType == null || myReturnType instanceof TheRUnknownType) {
      if (PsiManager.getInstance(myFunctionExpression.getProject()).isInProject(myFunctionExpression)) {
        TheRType type = TheRTypeProvider.guessReturnValueTypeFromBody(myFunctionExpression);
        if (!TheRUnknownType.class.isInstance(type)) {
          myReturnType = type;
        }
      }
    }
  }

  public void setOptional(String paramName) {
    myParameters.get(paramName).setOptional(true);
  }

  public TheRType getReturnType() {
    return myReturnType;
  }

  public List<TheRFunctionRule> getRules() {
    return myRules;
  }

  public void addParameterType(String name, TheRType type) {
    myParameters.get(name).setType(type);
  }

  public TheRType getParameterType(String name) {
    return myParameters.get(name).getType();
  }

  public void setReturnType(TheRType returnType) {
    myReturnType = returnType;
  }

  public TheRFunctionExpression getFunctionExpression() {
    return myFunctionExpression;
  }

  public void addRule(TheRFunctionRule rule) {
    myRules.add(0, rule); // we are parsing from bottom to top
  }

  public List<TheRParameter> getOptionalParams() {
    List<TheRParameter> optionalParams = new ArrayList<TheRParameter>();
    for (TheRTypedParameter typedParameter : myParameters.values()) {
      if (typedParameter.isOptional()) {
        optionalParams.add(typedParameter.getParameter());
      }
    }
    return optionalParams;
  }

  @SuppressWarnings("SimplifiableIfStatement")
  public boolean isOptional(String param) {
    if (param != null && myParameters.containsKey(param)) {
      return myParameters.get(param).isOptional();
    }
    return true;
  }
}