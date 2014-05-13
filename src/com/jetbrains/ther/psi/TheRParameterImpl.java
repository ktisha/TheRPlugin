package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import com.jetbrains.ther.psi.api.TheRParameter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRParameterImpl extends TheRElementImpl implements TheRParameter {
  public TheRParameterImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    return null; //TODO
  }

  @Override
  public String getName() {
    ASTNode node = getNameIdentifierNode();
    return node != null ? node.getText() : null;
  }


  @Nullable
  protected ASTNode getNameIdentifierNode() {
    return getNode().findChildByType(TheRTokenTypes.IDENTIFIER);
  }
}
