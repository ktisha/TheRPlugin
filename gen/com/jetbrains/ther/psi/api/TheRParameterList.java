// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface TheRParameterList extends TheRPsiElement {

  @NotNull
  List<TheRParameter> getParameterList();

  @NotNull
  PsiElement getLpar();

  @NotNull
  PsiElement getRpar();

}
