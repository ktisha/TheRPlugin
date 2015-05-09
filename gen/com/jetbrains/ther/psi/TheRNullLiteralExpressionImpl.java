// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.ther.psi.api.TheRNullLiteralExpression;
import com.jetbrains.ther.psi.api.TheRVisitor;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.parsing.TheRElementTypes.THE_R_NULL;

public class TheRNullLiteralExpressionImpl extends TheRExpressionImpl implements TheRNullLiteralExpression {

  public TheRNullLiteralExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitNullLiteralExpression(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getNull() {
    return findNotNullChildByType(THE_R_NULL);
  }

}
