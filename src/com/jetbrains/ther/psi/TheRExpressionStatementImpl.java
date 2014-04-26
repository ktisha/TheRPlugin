package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.psi.api.TheRExpressionStatement;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;
public class TheRExpressionStatementImpl extends TheRElementImpl implements TheRExpressionStatement {
  public TheRExpressionStatementImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }

  @Override
  @NotNull
  public TheRExpression getExpression() {
    throw new NotImplementedException();
  }
}
