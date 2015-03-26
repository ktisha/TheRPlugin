// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.ther.psi.api.TheRLogicalLiteralExpression;
import com.jetbrains.ther.psi.api.TheRVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.ther.parsing.TheRElementTypes.THE_R_FALSE;
import static com.jetbrains.ther.parsing.TheRElementTypes.THE_R_TRUE;

public class TheRLogicalLiteralExpressionImpl extends TheRExpressionImpl implements TheRLogicalLiteralExpression {

  public TheRLogicalLiteralExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitLogicalLiteralExpression(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getFalse() {
    return findChildByType(THE_R_FALSE);
  }

  @Override
  @Nullable
  public PsiElement getTrue() {
    return findChildByType(THE_R_TRUE);
  }

}
