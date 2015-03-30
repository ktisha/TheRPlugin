package com.jetbrains.ther.typing;

import com.jetbrains.ther.psi.api.TheRExpression;

public class TheRParameterConfiguration {

  private TheRType myType;
  private TheRExpression myValue;

  public TheRParameterConfiguration(TheRType type, TheRExpression value) {
    myType = type;
    myValue = value;
  }

  TheRType getType() {
    return myType;
  }

  TheRExpression getValue() {
    return myValue;
  }
}
