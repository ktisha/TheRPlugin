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

public class TheRBreakStatementImpl extends TheRExpressionImpl implements TheRBreakStatement {

  public TheRBreakStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitBreakStatement(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public TheRExpression getExpression() {
    return findChildByClass(TheRExpression.class);
  }

  @Override
  @Nullable
  public PsiElement getLpar() {
    return findChildByType(THE_R_LPAR);
  }

  @Override
  @Nullable
  public PsiElement getRpar() {
    return findChildByType(THE_R_RPAR);
  }

  @Override
  @NotNull
  public PsiElement getBreak() {
    return findNotNullChildByType(THE_R_BREAK);
  }

}
