// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;

public interface TheRAssignmentStatement extends TheRExpression, TheRNamedElement {

  boolean isLeft();

  TheRElement getAssignedValue();

  PsiElement getAssignee();

  String getName();

  PsiElement setName(String name);

  ASTNode getNameNode();

}
