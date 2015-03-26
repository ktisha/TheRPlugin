package com.jetbrains.ther.typing;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;

import java.util.List;

public class TheRTypeSequence implements TheRType {
  private List<TheRType> myTypes;

  public TheRTypeSequence(List<TheRType> types) {
    myTypes = types;
  }

  @Override
  public String getName() {
    return "type sequence: " + StringUtil.join(myTypes, new Function<TheRType, String>() {
      @Override
      public String fun(TheRType type) {
        return type.getName();
      }
    }, ",");
  }

  @Override
  public TheRType resolveType(TheRTypeEnvironment env) {
    return TheRMaxType.getMaxType(myTypes, env);
  }
}
