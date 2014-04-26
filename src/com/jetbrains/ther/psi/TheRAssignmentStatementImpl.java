package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import org.jetbrains.annotations.NotNull;

public class TheRAssignmentStatementImpl extends TheRElementImpl implements TheRAssignmentStatement {
  public TheRAssignmentStatementImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }

}
