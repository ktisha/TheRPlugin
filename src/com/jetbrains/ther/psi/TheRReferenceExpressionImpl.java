package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRReferenceExpression;

public class TheRReferenceExpressionImpl extends TheRElementImpl implements TheRReferenceExpression {
  public TheRReferenceExpressionImpl(ASTNode astNode) {
    super(astNode);
  }
}
