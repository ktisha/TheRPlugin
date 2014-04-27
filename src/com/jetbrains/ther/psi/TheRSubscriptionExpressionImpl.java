package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRSubscriptionExpression;
import org.jetbrains.annotations.NotNull;

public class TheRSubscriptionExpressionImpl extends TheRElementImpl implements TheRSubscriptionExpression {
  public TheRSubscriptionExpressionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
