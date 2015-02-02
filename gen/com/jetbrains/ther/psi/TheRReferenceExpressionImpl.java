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
import com.jetbrains.ther.psi.references.TheRReferenceImpl;

public class TheRReferenceExpressionImpl extends TheRExpressionImpl implements TheRReferenceExpression {

  public TheRReferenceExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitReferenceExpression(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getFalse() {
    return findChildByType(THE_R_FALSE);
  }

  @Override
  @Nullable
  public PsiElement getInf() {
    return findChildByType(THE_R_INF);
  }

  @Override
  @Nullable
  public PsiElement getNa() {
    return findChildByType(THE_R_NA);
  }

  @Override
  @Nullable
  public PsiElement getNan() {
    return findChildByType(THE_R_NAN);
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
  public PsiElement getNull() {
    return findChildByType(THE_R_NULL);
  }

  @Override
  @Nullable
  public PsiElement getTrue() {
    return findChildByType(THE_R_TRUE);
  }

  @Override
  @Nullable
  public PsiElement getIdentifier() {
    return findChildByType(THE_R_IDENTIFIER);
  }

  public TheRReferenceImpl getReference() {
    return TheRPsiImplUtil.getReference(this);
  }

  @Nullable
  public String getNamespace() {
    return TheRPsiImplUtil.getNamespace(this);
  }

  public String getName() {
    return TheRPsiImplUtil.getName(this);
  }

}
