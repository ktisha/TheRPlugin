package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRBinaryExpression;
import org.jetbrains.annotations.NotNull;

public class TheRBinaryExpressionImpl extends TheRElementImpl implements TheRBinaryExpression {
  public TheRBinaryExpressionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
