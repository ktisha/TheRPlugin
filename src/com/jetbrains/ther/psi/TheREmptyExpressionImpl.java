package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheREmptyExpression;

public class TheREmptyExpressionImpl extends TheRElementImpl implements TheREmptyExpression {
  public TheREmptyExpressionImpl(ASTNode astNode) {
    super(astNode);
  }
}
