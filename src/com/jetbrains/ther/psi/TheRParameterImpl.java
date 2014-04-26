package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRParameter;
import org.jetbrains.annotations.NotNull;

public class TheRParameterImpl extends TheRElementImpl implements TheRParameter {
  public TheRParameterImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
