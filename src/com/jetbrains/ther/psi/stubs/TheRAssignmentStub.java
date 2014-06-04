package com.jetbrains.ther.psi.stubs;

import com.intellij.psi.stubs.NamedStub;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;

public interface TheRAssignmentStub extends NamedStub<TheRAssignmentStatement> {
  public boolean isFunctionDeclaration();
}
