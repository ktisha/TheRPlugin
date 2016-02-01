package com.jetbrains.ther.typing.types;

import com.jetbrains.ther.typing.TheRTypeEnvironment;

public class TheRTypeVariable extends TheRType {
  private final String myVarName;

  @Override
  public String getCanonicalName() {
    return myVarName;
  }

  @Override
  public TheRType resolveType(TheRTypeEnvironment env) {
    TheRType substitutionType = env.getType(myVarName);
    substitutionType = substitutionType.replaceS3Types(getS3Classes()); // push down s3
    return substitutionType.resolveType(env);
  }

  public TheRTypeVariable(String name) {
    myVarName = name;
  }
}
