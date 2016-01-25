package com.jetbrains.ther.typing.types;

import com.jetbrains.ther.psi.api.TheRExpression;

import java.util.HashMap;
import java.util.Map;

public class TheRFunctionRule {

  private TheRType myReturnType;
  private Map<String, TheRParameterConfiguration> myParameters = new HashMap<String, TheRParameterConfiguration>();

  public TheRFunctionRule(TheRType returnType) {
    myReturnType = returnType;
  }


  public Map<String, TheRParameterConfiguration> getParameters() {
    return myParameters;
  }

  public void addParameter(String name, TheRType type, TheRExpression value) {
    myParameters.put(name, new TheRParameterConfiguration(type, value));
  }

  public TheRType getReturnType() {
    return myReturnType;
  }
}
