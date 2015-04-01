package com.jetbrains.ther.typing.types;

import com.jetbrains.ther.typing.TheRTypeEnvironment;

public class TheRTypeVariable extends TheRType {
  private final String myVarName;

  @Override
  public String getName() {
    return myVarName;
  }

  @Override
  public TheRType resolveType(TheRTypeEnvironment env) {
    return env.getType(myVarName).resolveType(env);
  }

  public TheRTypeVariable(String name) {
    myVarName = name;
  }
}
