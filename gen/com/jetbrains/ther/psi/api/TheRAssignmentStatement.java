// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

public interface TheRAssignmentStatement extends TheRExpression, TheRNamedElement {

  boolean isLeft();

  TheRPsiElement getAssignedValue();

  PsiElement getAssignee();

  String getName();

  PsiElement setName(String name);

  ASTNode getNameNode();

}
