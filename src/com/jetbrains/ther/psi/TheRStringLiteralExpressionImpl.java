package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class TheRStringLiteralExpressionImpl extends TheRElementImpl implements TheRStringLiteralExpression {
  public TheRStringLiteralExpressionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
