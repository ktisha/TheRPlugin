package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;

public class TheRElementImpl extends TheRBaseElementImpl<StubElement> {
  public TheRElementImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }
}
