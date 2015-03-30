package com.jetbrains.ther.psi.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.jetbrains.ther.psi.TheRBaseElementImpl;
import com.jetbrains.ther.psi.api.TheRExpression;
import org.jetbrains.annotations.NotNull;

public abstract class TheRAssignmentBase extends TheRBaseElementImpl<TheRAssignmentStub> implements TheRExpression {

  public TheRAssignmentBase(@NotNull com.intellij.lang.ASTNode node) {
    super(node);
  }

  public TheRAssignmentBase(@NotNull TheRAssignmentStub stub, @NotNull IStubElementType nodeType) {
    super(stub, nodeType);
  }
}
