package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRPrefixExpression;
import org.jetbrains.annotations.NotNull;

public class TheRPrefixExpressionImpl extends TheRElementImpl implements TheRPrefixExpression {
  public TheRPrefixExpressionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
