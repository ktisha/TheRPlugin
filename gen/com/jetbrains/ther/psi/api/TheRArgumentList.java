// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface TheRArgumentList extends TheRElement {

  @NotNull
  List<TheRExpression> getExpressionList();

  @NotNull
  PsiElement getLpar();

  @NotNull
  PsiElement getRpar();

}
