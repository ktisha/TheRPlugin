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

public class TheRBlockExpressionImpl extends TheRExpressionImpl implements TheRBlockExpression {

  public TheRBlockExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitBlockExpression(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<TheRExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TheRExpression.class);
  }

  @Override
  @NotNull
  public PsiElement getLbrace() {
    return findNotNullChildByType(THE_R_LBRACE);
  }

  @Override
  @NotNull
  public PsiElement getRbrace() {
    return findNotNullChildByType(THE_R_RBRACE);
  }

}
