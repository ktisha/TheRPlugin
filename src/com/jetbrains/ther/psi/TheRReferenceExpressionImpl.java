package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.jetbrains.ther.psi.api.TheRReferenceExpression;
import com.jetbrains.ther.psi.references.TheRReferenceImpl;

public class TheRReferenceExpressionImpl extends TheRElementImpl implements TheRReferenceExpression {
  public TheRReferenceExpressionImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public PsiReference getReference() {
    return new TheRReferenceImpl(this);
  }
}
