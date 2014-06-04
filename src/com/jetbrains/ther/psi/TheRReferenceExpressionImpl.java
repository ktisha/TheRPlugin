package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import com.jetbrains.ther.psi.api.TheRReferenceExpression;
import com.jetbrains.ther.psi.references.TheRReferenceImpl;
import org.jetbrains.annotations.Nullable;

public class TheRReferenceExpressionImpl extends TheRElementImpl implements TheRReferenceExpression {
  public TheRReferenceExpressionImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public PsiReference getReference() {
    final PsiElement nextElement = PsiTreeUtil.skipSiblingsForward(this, PsiWhiteSpace.class);
    if (nextElement != null && TheRTokenTypes.LEFT_ASSIGNMENTS.contains(nextElement.getNode().getElementType())) return null;
    final PsiElement prevElement = PsiTreeUtil.skipSiblingsBackward(this, PsiWhiteSpace.class);
    if (prevElement != null && TheRTokenTypes.RIGHT_ASSIGNMENTS.contains(prevElement.getNode().getElementType())) return null;
    return new TheRReferenceImpl(this);
  }

  @Override
  @Nullable
  public String getNamespace() {
    final String text = getText();
    final int namespaceIndex = text.indexOf("::");
    if (namespaceIndex > 0) {
      return text.substring(0, namespaceIndex);
    }
    return null;
  }

  @Override
  public String getName() {
    final String text = getText();
    final int namespaceIndex = text.indexOf("::");
    if (namespaceIndex > 0) {
      return text.substring(namespaceIndex + 2);
    }
    return text;
  }
}
