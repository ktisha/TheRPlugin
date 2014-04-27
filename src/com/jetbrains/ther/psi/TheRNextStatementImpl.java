package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRNextStatement;
import org.jetbrains.annotations.NotNull;

public class TheRNextStatementImpl extends TheRElementImpl implements TheRNextStatement {
  public TheRNextStatementImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
