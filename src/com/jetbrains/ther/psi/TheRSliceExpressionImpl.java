package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRSliceExpression;
import org.jetbrains.annotations.NotNull;

public class TheRSliceExpressionImpl extends TheRElementImpl implements TheRSliceExpression {
  public TheRSliceExpressionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
