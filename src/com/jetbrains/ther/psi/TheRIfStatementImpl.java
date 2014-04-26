package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRIfStatement;
import org.jetbrains.annotations.NotNull;

public class TheRIfStatementImpl extends TheRElementImpl implements TheRIfStatement {
  public TheRIfStatementImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
