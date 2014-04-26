package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRBlock;
import org.jetbrains.annotations.NotNull;

public class TheRBlockImpl extends TheRElementImpl implements TheRBlock {
  public TheRBlockImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
