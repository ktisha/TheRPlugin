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

public class TheRHelpExpressionImpl extends TheRExpressionImpl implements TheRHelpExpression {

  public TheRHelpExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitHelpExpression(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public TheRExpression getExpression() {
    return findChildByClass(TheRExpression.class);
  }

  @Override
  @Nullable
  public PsiElement getNaCharacter() {
    return findChildByType(THE_R_NA_CHARACTER);
  }

  @Override
  @Nullable
  public PsiElement getNaComplex() {
    return findChildByType(THE_R_NA_COMPLEX);
  }

  @Override
  @Nullable
  public PsiElement getNaInteger() {
    return findChildByType(THE_R_NA_INTEGER);
  }

  @Override
  @Nullable
  public PsiElement getNaReal() {
    return findChildByType(THE_R_NA_REAL);
  }

  @Override
  @Nullable
  public PsiElement getTripleDots() {
    return findChildByType(THE_R_TRIPLE_DOTS);
  }

  @Override
  @Nullable
  public PsiElement getBreak() {
    return findChildByType(THE_R_BREAK);
  }

  @Override
  @Nullable
  public PsiElement getElse() {
    return findChildByType(THE_R_ELSE);
  }

  @Override
  @Nullable
  public PsiElement getFor() {
    return findChildByType(THE_R_FOR);
  }

  @Override
  @Nullable
  public PsiElement getFunction() {
    return findChildByType(THE_R_FUNCTION);
  }

  @Override
  @Nullable
  public PsiElement getIf() {
    return findChildByType(THE_R_IF);
  }

  @Override
  @Nullable
  public PsiElement getIn() {
    return findChildByType(THE_R_IN);
  }

  @Override
  @Nullable
  public PsiElement getNext() {
    return findChildByType(THE_R_NEXT);
  }

  @Override
  @Nullable
  public PsiElement getRepeat() {
    return findChildByType(THE_R_REPEAT);
  }

  @Override
  @Nullable
  public PsiElement getWhile() {
    return findChildByType(THE_R_WHILE);
  }

}
