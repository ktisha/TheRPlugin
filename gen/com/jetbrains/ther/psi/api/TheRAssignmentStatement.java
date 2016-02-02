// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.jetbrains.ther.psi.stubs.TheRAssignmentStub;

public interface TheRAssignmentStatement extends TheRNamedElement, StubBasedPsiElement<TheRAssignmentStub> {

  boolean isLeft();

  boolean isRight();

  boolean isEqual();

  TheRPsiElement getAssignedValue();

  PsiElement getAssignee();

  String getName();

  PsiElement setName(String name);

  ASTNode getNameNode();

}
