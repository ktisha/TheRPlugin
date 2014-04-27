package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRReprExpression;
import org.jetbrains.annotations.NotNull;

public class TheRReprExpressionImpl extends TheRElementImpl implements TheRReprExpression {
  public TheRReprExpressionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
