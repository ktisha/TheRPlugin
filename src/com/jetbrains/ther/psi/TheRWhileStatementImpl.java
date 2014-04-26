package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRWhileStatement;
import org.jetbrains.annotations.NotNull;

public class TheRWhileStatementImpl extends TheRElementImpl implements TheRWhileStatement {
  public TheRWhileStatementImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
