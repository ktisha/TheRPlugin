// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TheRArgumentList extends TheRPsiElement {

  @NotNull
  List<TheRExpression> getExpressionList();

  @NotNull
  PsiElement getLpar();

  @NotNull
  PsiElement getRpar();

}
