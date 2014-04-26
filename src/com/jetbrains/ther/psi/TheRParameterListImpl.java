package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRParameterList;
import org.jetbrains.annotations.NotNull;

public class TheRParameterListImpl extends TheRElementImpl implements TheRParameterList {
  public TheRParameterListImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
