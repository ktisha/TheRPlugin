package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRRepeatStatement;
import org.jetbrains.annotations.NotNull;

public class TheRRepeatStatementImpl extends TheRElementImpl implements TheRRepeatStatement {
  public TheRRepeatStatementImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
