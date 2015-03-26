package com.jetbrains.ther.typing;


import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;

import java.util.List;

public class TheRMaxType implements TheRType {

  private List<TheRType> myTypes;

  public TheRMaxType(List<TheRType> types) {
    myTypes = types;
  }

  @Override
  public String getName() {
    return "max type of" + StringUtil.join(myTypes, new Function<TheRType, String>() {
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

  public static TheRType getMaxType(List<TheRType> types, TheRTypeEnvironment env) {
    TheRType maxType = null;
    for (TheRType type : types) {
      TheRType resolvedType = type.resolveType(env);
      if (maxType == null || getOrder(resolvedType) > getOrder(maxType)) {
        maxType = resolvedType;
      }
    }
    return maxType;
  }

  public static int getOrder(TheRType type) {
    if (type == TheRLogicalType.INSTANCE) {
      return 0;
    } else if (type == TheRNumericType.INSTANCE) {
      return 1;
    } else if (type == TheRCharacterType.INSTANCE) {
      return 2;
    } else if (type == TheRType.UNKNOWN) {
      return 3;
    } else {
      throw new IllegalArgumentException("Incorrect type: " + type.getName());
    }
  }
}
