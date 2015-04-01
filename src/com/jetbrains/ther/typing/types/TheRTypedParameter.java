package com.jetbrains.ther.typing.types;

import com.jetbrains.ther.psi.api.TheRParameter;
import com.jetbrains.ther.typing.types.TheRType;

public class TheRTypedParameter {
  String myName;
  TheRType myType;
  TheRParameter myParameter;

  public TheRTypedParameter(String name, TheRType type, TheRParameter parameter) {
    myName = name;
    myType = type;
    myParameter = parameter;
  }

  public String getName() {
    return myName;
  }

  public TheRType getType() {
    return myType;
  }

  public TheRParameter getParameter() {
    return myParameter;
  }

  public void setType(TheRType type) {
    myType = type;
  }
}
