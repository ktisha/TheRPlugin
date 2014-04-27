package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRBreakStatement;
import org.jetbrains.annotations.NotNull;

public class TheRBreakStatementImpl extends TheRElementImpl implements TheRBreakStatement {
  public TheRBreakStatementImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
