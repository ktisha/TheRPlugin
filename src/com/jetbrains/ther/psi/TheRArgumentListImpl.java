package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.api.TheRArgumentList;
import org.jetbrains.annotations.NotNull;

public class TheRArgumentListImpl extends TheRElementImpl implements TheRArgumentList {
  public TheRArgumentListImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
