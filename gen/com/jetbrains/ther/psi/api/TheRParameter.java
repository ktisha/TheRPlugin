// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;

public interface TheRParameter extends TheRNamedElement {

  @Nullable
  TheRExpression getExpression();

  @Nullable
  PsiElement getEq();

  @Nullable
  PsiElement getIdentifier();

  ASTNode getNameNode();

  String getName();

  PsiElement setName(String name);

}
