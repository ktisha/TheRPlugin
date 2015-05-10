// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.ther.psi.api.TheRBinaryOperator;
import com.jetbrains.ther.psi.api.TheRVisitor;
import com.jetbrains.ther.psi.references.TheRBinaryOperatorReference;
import org.jetbrains.annotations.NotNull;

public class TheRBinaryOperatorImpl extends TheRElementImpl implements TheRBinaryOperator {

  public TheRBinaryOperatorImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitBinaryOperator(this);
    else super.accept(visitor);
  }

  public String getName() {
    return TheRPsiImplUtil.getName(this);
  }

  public TheRBinaryOperatorReference getReference() {
    return TheRPsiImplUtil.getReference(this);
  }

}
