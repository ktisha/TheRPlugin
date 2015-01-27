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

public class TheRSubscriptionExpressionImpl extends TheRExpressionImpl implements TheRSubscriptionExpression {

  public TheRSubscriptionExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitSubscriptionExpression(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<TheRExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TheRExpression.class);
  }

  @Override
  @Nullable
  public PsiElement getLbracket() {
    return findChildByType(THE_R_LBRACKET);
  }

  @Override
  @Nullable
  public PsiElement getLdbracket() {
    return findChildByType(THE_R_LDBRACKET);
  }

  @Override
  @Nullable
  public PsiElement getRbracket() {
    return findChildByType(THE_R_RBRACKET);
  }

  @Override
  @Nullable
  public PsiElement getRdbracket() {
    return findChildByType(THE_R_RDBRACKET);
  }

}
