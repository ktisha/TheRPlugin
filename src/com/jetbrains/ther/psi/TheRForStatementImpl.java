package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRForStatement;
import org.jetbrains.annotations.NotNull;

public class TheRForStatementImpl extends TheRElementImpl implements TheRForStatement {
  public TheRForStatementImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
