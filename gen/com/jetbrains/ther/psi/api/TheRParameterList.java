// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TheRParameterList extends TheRPsiElement {

  @NotNull
  List<TheRParameter> getParameterList();

  @NotNull
  PsiElement getLpar();

  @NotNull
  PsiElement getRpar();

}
