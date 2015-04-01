package com.jetbrains.ther.typing.types;

import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.typing.types.TheRType;

public class TheRParameterConfiguration {

  private TheRType myType;
  private TheRExpression myValue;

  public TheRParameterConfiguration(TheRType type, TheRExpression value) {
    myType = type;
    myValue = value;
  }

  public TheRType getType() {
    return myType;
  }

  public TheRExpression getValue() {
    return myValue;
  }
}
