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

public class TheRSliceExpressionImpl extends TheRExpressionImpl implements TheRSliceExpression {

  public TheRSliceExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitSliceExpression(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<TheRExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TheRExpression.class);
  }

  @Override
  @Nullable
  public TheROperator getOperator() {
    return findChildByClass(TheROperator.class);
  }

}
