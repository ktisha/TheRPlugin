// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.jetbrains.ther.psi.stubs.TheRAssignmentStub;
import com.intellij.lang.ASTNode;

public interface TheRAssignmentStatement extends TheRNamedElement, StubBasedPsiElement<TheRAssignmentStub> {

  boolean isLeft();

  boolean isRight();

  TheRPsiElement getAssignedValue();

  PsiElement getAssignee();

  String getName();

  PsiElement setName(String name);

  ASTNode getNameNode();

}
