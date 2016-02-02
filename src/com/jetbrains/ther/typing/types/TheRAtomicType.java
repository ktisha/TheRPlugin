package com.jetbrains.ther.typing.types;

import com.jetbrains.ther.psi.api.TheRExpression;

import java.util.List;

public abstract class TheRAtomicType extends TheRType {
  @Override
  public TheRType getSubscriptionType(List<TheRExpression> expressions, boolean isSingleBracket) {
    return this;
  }

  @Override
  public TheRType afterSubscriptionType(List<TheRExpression> arguments, TheRType valueType, boolean isSingle) {
    if (arguments.isEmpty()) {
      return this;
    }
    if (valueType instanceof TheRAtomicType) {
      return TheRType.getOrder(this) > TheRType.getOrder(valueType) ? this : valueType;
    }
    return super.afterSubscriptionType(arguments, valueType, isSingle);
  }

  @Override
  public TheRType getElementTypes() {
    return this;
  }

  @Override
  public TheRType getMemberType(String tag) {
    return new TheRErrorType("$ operator is invalid for atomic vectors");
  }
}
