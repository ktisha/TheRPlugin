// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.jetbrains.ther.typing.types.TheRType;

public interface TheRNaLiteralExpression extends TheRExpression {

  @Nullable
  PsiElement getNa();

  @Nullable
  PsiElement getNaCharacter();

  @Nullable
  PsiElement getNaComplex();

  @Nullable
  PsiElement getNaInteger();

  @Nullable
  PsiElement getNaReal();

  TheRType getType();

}
