package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.stubs.TheRAssignmentStub;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRAssignmentStatementImpl extends TheRBaseElementImpl<TheRAssignmentStub> implements TheRAssignmentStatement {
  public TheRAssignmentStatementImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }

  public TheRAssignmentStatementImpl(@NotNull final TheRAssignmentStub stub) {
    super(stub, TheRElementTypes.ASSIGNMENT_STATEMENT);
  }

  @Nullable
  @Override
  public String getName() {
    final TheRAssignmentStub stub = getStub();
    if (stub != null) {
      return stub.getName();
    }

    final ASTNode node = getNameNode();
    return node != null ? node.getText() : null;
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    throw new NotImplementedException();
  }

  @Nullable
  public ASTNode getNameNode() {
    final ASTNode node = getNode();
    if (isLeft()) {
      return node.findChildByType(TheRElementTypes.REFERENCE_EXPRESSION);
    }
    for(ASTNode element = node.getLastChildNode(); element != null; element = element.getTreePrev()){
      if (element.getElementType() == TheRElementTypes.REFERENCE_EXPRESSION) return element;
    }
    return null;
  }

  @Override
  @Nullable
  public PsiElement getAssignee() {
    final ASTNode nameNode = getNameNode();
    return nameNode != null ? nameNode.getPsi() : null;
  }

  private boolean isLeft() {
    final ASTNode operator = getNode().findChildByType(TheRTokenTypes.LEFT_ASSIGNMENTS);
    return operator != null;
  }
}
