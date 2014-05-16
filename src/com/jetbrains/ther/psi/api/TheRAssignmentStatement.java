package com.jetbrains.ther.psi.api;

import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import com.jetbrains.ther.psi.stubs.TheRAssignmentStub;

public interface TheRAssignmentStatement extends TheRStatement, PsiNamedElement, StubBasedPsiElement<TheRAssignmentStub> {
}
