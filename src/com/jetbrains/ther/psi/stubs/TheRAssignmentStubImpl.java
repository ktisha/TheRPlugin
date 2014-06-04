package com.jetbrains.ther.psi.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRAssignmentStubImpl extends StubBase<TheRAssignmentStatement> implements TheRAssignmentStub {
  private final String myName;
  private final boolean isFunction;

  public TheRAssignmentStubImpl(@Nullable final String name,
                                @NotNull final StubElement parent,
                                @NotNull IStubElementType stubElementType,
                                boolean isFunctionDefinition) {
    super(parent, stubElementType);
    myName = name;
    isFunction = isFunctionDefinition;
  }

  @Override
  public String getName() {
    return myName;
  }

  @Override
  public String toString() {
    return "TheRAssignmentStub(" + myName + ")";
  }

  @Override
  public boolean isFunctionDeclaration() {
    return isFunction;
  }
}
