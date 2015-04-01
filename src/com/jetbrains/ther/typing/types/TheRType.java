package com.jetbrains.ther.typing.types;

import com.jetbrains.ther.typing.TheRTypeEnvironment;

import java.util.List;

public abstract class TheRType {
  public static TheRType UNKNOWN = new TheRType() {
    @Override
    public String getName() {
      return "Unknown";
    }

    @Override
    public TheRType resolveType(TheRTypeEnvironment env) {
      return this;
    }
  };

  public static TheRType getMaxType(List<TheRType> types, TheRTypeEnvironment env) {
    TheRType maxType = null;
    for (TheRType type : types) {
      TheRType resolvedType = type.resolveType(env);
      if (maxType == null || TheRType.getOrder(resolvedType) > TheRType.getOrder(maxType)) {
        maxType = resolvedType;
      }
    }
    return maxType;
  }

  public static int getOrder(TheRType type) {
    if (type == TheRRawType.INSTANCE) {
      return 0;
    } else if (type == TheRLogicalType.INSTANCE){
      return 1;
    } else if (type == TheRIntegerType.INSTANCE) {
      return 2;
    } else if (type == TheRNumericType.INSTANCE) {
      return 3;
    } else if (type == TheRComplexType.INSTANCE) {
      return 4;
    } else if (type == TheRCharacterType.INSTANCE) {
      return 5;
    } else if (type == TheRType.UNKNOWN) {
      return 6;
    } else {
      throw new IllegalArgumentException("Incorrect type: " + type.getName());
    }
  }

  public abstract String getName();

  @SuppressWarnings("SimplifiableIfStatement")
  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof TheRType)) {
      return false;
    }
    return ((TheRType)o).getName().equals(getName());
  }

  public TheRType resolveType(TheRTypeEnvironment env) {
    return this;
  }

  public TheRType getSubscriptionType() {
    return TheRType.UNKNOWN;
  }
}
