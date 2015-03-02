// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TheRFunctionExpression extends TheRExpression {

  @Nullable
  TheRExpression getExpression();

  @NotNull
  TheRParameterList getParameterList();

  @NotNull
  PsiElement getFunction();

  String getDocStringValue();

}
