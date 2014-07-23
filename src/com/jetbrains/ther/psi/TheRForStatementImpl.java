package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.psi.api.TheRForStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRForStatementImpl extends TheRElementImpl implements TheRForStatement {
  public TheRForStatementImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }

  @Nullable
  @Override
  public TheRExpression getTarget() {
    final PsiElement lPar = findChildByType(TheRTokenTypes.LPAR);
    if (lPar != null) {
      final PsiElement element = PsiTreeUtil.skipSiblingsForward(lPar, PsiWhiteSpace.class);
      return element instanceof TheRExpression ? (TheRExpression)element : null;
    }
    return null;
  }
}
