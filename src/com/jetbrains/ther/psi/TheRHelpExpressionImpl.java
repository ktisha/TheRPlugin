package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRHelpExpression;

public class TheRHelpExpressionImpl extends TheRElementImpl implements TheRHelpExpression {
  public TheRHelpExpressionImpl(ASTNode astNode) {
    super(astNode);
  }
}
