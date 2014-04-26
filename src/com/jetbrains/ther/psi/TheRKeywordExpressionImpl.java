package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRKeywordExpression;
import org.jetbrains.annotations.NotNull;

public class TheRKeywordExpressionImpl extends TheRElementImpl implements TheRKeywordExpression {
  public TheRKeywordExpressionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
