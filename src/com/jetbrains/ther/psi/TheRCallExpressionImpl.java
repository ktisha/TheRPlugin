package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRCallExpression;
import org.jetbrains.annotations.NotNull;

public class TheRCallExpressionImpl extends TheRElementImpl implements TheRCallExpression {
  public TheRCallExpressionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
