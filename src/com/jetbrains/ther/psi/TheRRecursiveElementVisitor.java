package com.jetbrains.ther.psi;

import com.intellij.psi.PsiElement;
import com.jetbrains.ther.psi.api.TheRVisitor;

public class TheRRecursiveElementVisitor extends TheRVisitor {
  @Override
  public void visitElement(PsiElement element) {
    element.acceptChildren(this);
  }
}
