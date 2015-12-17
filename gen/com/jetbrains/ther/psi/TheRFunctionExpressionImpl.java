// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.psi.api.TheRFunctionExpression;
import com.jetbrains.ther.psi.api.TheRParameterList;
import com.jetbrains.ther.psi.api.TheRVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.ther.parsing.TheRElementTypes.THE_R_FUNCTION;

public class TheRFunctionExpressionImpl extends TheRExpressionImpl implements TheRFunctionExpression {

  public TheRFunctionExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitFunctionExpression(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public TheRExpression getExpression() {
    return findChildByClass(TheRExpression.class);
  }

  @Override
  @NotNull
  public TheRParameterList getParameterList() {
    return findNotNullChildByClass(TheRParameterList.class);
  }

  @Override
  @NotNull
  public PsiElement getFunction() {
    return findNotNullChildByType(THE_R_FUNCTION);
  }

  @Nullable
  public String getDocStringValue() {
    return TheRPsiImplUtil.getDocStringValue(this);
  }

}
