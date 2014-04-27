package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheROperatorExpression;
import org.jetbrains.annotations.NotNull;

public class TheROperatorExpressionImpl extends TheRElementImpl implements TheROperatorExpression {
  public TheROperatorExpressionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
