// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface TheRSubscriptionExpression extends TheRExpression {

  @NotNull
  List<TheRExpression> getExpressionList();

  @Nullable
  PsiElement getLbracket();

  @Nullable
  PsiElement getLdbracket();

  @Nullable
  PsiElement getRbracket();

  @Nullable
  PsiElement getRdbracket();

}
