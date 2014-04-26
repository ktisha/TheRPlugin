package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRNumericLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class TheRNumericLiteralExpressionImpl extends TheRElementImpl implements TheRNumericLiteralExpression {
  public TheRNumericLiteralExpressionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
