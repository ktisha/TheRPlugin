package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRParenthesizedExpression;
import org.jetbrains.annotations.NotNull;

public class TheRParenthesizedExpressionImpl extends TheRElementImpl implements TheRParenthesizedExpression {
  public TheRParenthesizedExpressionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
