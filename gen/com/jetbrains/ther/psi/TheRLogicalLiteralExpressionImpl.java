// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.ther.parsing.TheRElementTypes.*;
import com.jetbrains.ther.psi.api.*;

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
  public PsiElement getF() {
    return findChildByType(THE_R_F);
  }

  @Override
  @Nullable
  public PsiElement getFalse() {
    return findChildByType(THE_R_FALSE);
  }

  @Override
  @Nullable
  public PsiElement getT() {
    return findChildByType(THE_R_T);
  }

  @Override
  @Nullable
  public PsiElement getTrue() {
    return findChildByType(THE_R_TRUE);
  }

}
