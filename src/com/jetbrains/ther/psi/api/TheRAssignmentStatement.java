package com.jetbrains.ther.psi.api;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import com.jetbrains.ther.psi.stubs.TheRAssignmentStub;
import org.jetbrains.annotations.Nullable;

public interface TheRAssignmentStatement extends TheRStatement, PsiNamedElement, StubBasedPsiElement<TheRAssignmentStub> {
  @Nullable
  public PsiElement getAssignee();

  @Nullable
  public TheRElement getAssignedValue();
}
