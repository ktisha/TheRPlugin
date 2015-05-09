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

public class TheRForStatementImpl extends TheRExpressionImpl implements TheRForStatement {

  public TheRForStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitForStatement(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<TheRExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TheRExpression.class);
  }

  @Override
  @NotNull
  public PsiElement getLpar() {
    return findNotNullChildByType(THE_R_LPAR);
  }

  @Override
  @NotNull
  public PsiElement getRpar() {
    return findNotNullChildByType(THE_R_RPAR);
  }

  @Override
  @NotNull
  public PsiElement getFor() {
    return findNotNullChildByType(THE_R_FOR);
  }

  @Override
  @NotNull
  public TheRExpression getTarget() {
    List<TheRExpression> p1 = getExpressionList();
    return p1.get(0);
  }

  @Override
  @Nullable
  public TheRExpression getRange() {
    List<TheRExpression> p1 = getExpressionList();
    return p1.size() < 2 ? null : p1.get(1);
  }

  @Override
  @Nullable
  public TheRExpression getBody() {
    List<TheRExpression> p1 = getExpressionList();
    return p1.size() < 3 ? null : p1.get(2);
  }

}
