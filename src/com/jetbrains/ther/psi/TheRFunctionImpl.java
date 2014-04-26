package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRFunction;
import org.jetbrains.annotations.NotNull;

public class TheRFunctionImpl extends TheRElementImpl implements TheRFunction {
  public TheRFunctionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
