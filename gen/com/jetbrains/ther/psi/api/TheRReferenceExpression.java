// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import com.intellij.psi.PsiElement;
import com.jetbrains.ther.psi.references.TheRReferenceImpl;
import org.jetbrains.annotations.Nullable;

public interface TheRReferenceExpression extends TheRExpression {

  @Nullable
  PsiElement getInf();

  @Nullable
  PsiElement getNa();

  @Nullable
  PsiElement getNan();

  @Nullable
  PsiElement getNaCharacter();

  @Nullable
  PsiElement getNaComplex();

  @Nullable
  PsiElement getNaInteger();

  @Nullable
  PsiElement getNaReal();

  @Nullable
  PsiElement getIdentifier();

  TheRReferenceImpl getReference();

  @Nullable
  String getNamespace();

  String getName();

}
