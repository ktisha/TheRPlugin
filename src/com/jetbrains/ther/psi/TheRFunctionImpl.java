package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRFunction;
import com.jetbrains.ther.psi.api.TheRParameterList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRFunctionImpl extends TheRElementImpl implements TheRFunction {
  public TheRFunctionImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    return null;//TODO
  }

  @Nullable
  @Override
  public String getName() {
    final PsiElement parent = getParent();
    if (parent instanceof TheRAssignmentStatement) {
      ASTNode node = getNameNode();
      return node != null ? node.getText() : null;
    }
    return null;
  }

  @Nullable
  public ASTNode getNameNode() {
    return getParent().getNode().findChildByType(TheRElementTypes.REFERENCE_EXPRESSION);
  }

  @NotNull
  @Override
  public TheRParameterList getParameterList() {
    final ASTNode childNode = getNode().findChildByType(TheRElementTypes.PARAMETER_LIST);
    assert childNode != null : "Missing required parameter list";
    return (TheRParameterList)childNode.getPsi();
  }
}
