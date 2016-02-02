// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.jetbrains.ther.psi.references.TheRReferenceImpl;

public interface TheRReferenceExpression extends TheRExpression {

  @Nullable
  PsiElement getInf();

  @Nullable
  PsiElement getNan();

  @Nullable
  PsiElement getIdentifier();

  TheRReferenceImpl getReference();

  @Nullable
  String getNamespace();

  String getName();

}
