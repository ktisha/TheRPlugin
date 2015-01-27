// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface TheRFunctionExpression extends TheRExpression {

  @Nullable
  TheRExpression getExpression();

  @NotNull
  TheRParameterList getParameterList();

  @NotNull
  PsiElement getFunction();

}
