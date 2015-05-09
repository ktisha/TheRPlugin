// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TheRMemberExpression extends TheRExpression {

  @NotNull
  TheRExpression getExpression();

  @NotNull
  PsiElement getListSubset();

  @Nullable
  PsiElement getIdentifier();

  @Nullable
  PsiElement getString();

  String getTag();

}
