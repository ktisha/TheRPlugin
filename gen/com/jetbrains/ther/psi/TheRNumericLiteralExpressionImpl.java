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

public class TheRNumericLiteralExpressionImpl extends TheRExpressionImpl implements TheRNumericLiteralExpression {

  public TheRNumericLiteralExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitNumericLiteralExpression(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getComplex() {
    return findChildByType(THE_R_COMPLEX);
  }

  @Override
  @Nullable
  public PsiElement getInteger() {
    return findChildByType(THE_R_INTEGER);
  }

  @Override
  @Nullable
  public PsiElement getNumeric() {
    return findChildByType(THE_R_NUMERIC);
  }

}
