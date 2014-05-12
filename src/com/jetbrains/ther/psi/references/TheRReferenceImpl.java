package com.jetbrains.ther.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRReferenceImpl implements PsiReference, PsiPolyVariantReference {
  protected final TheRElement myElement;

  public TheRReferenceImpl(TheRElement element) {
    myElement = element;
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    return new ResolveResult[0];
  }

  @Override
  public PsiElement getElement() {
    return myElement;
  }

  @Override
  public TextRange getRangeInElement() {
    final TextRange range = myElement.getNode().getTextRange();
    return range.shiftRight(-myElement.getNode().getStartOffset());
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final String name = myElement.getText();
    if (name != null) {
      final PsiFile file = myElement.getContainingFile();
      final TheRAssignmentStatement[] statements = PsiTreeUtil.getChildrenOfType(file, TheRAssignmentStatement.class);
      if (statements != null) {
        for (TheRAssignmentStatement statement : statements) {
          final PsiElement child = statement.getFirstChild();
          if (child.getText().equals(name)) return statement;
        }
      }
    }
    return null;
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return getElement().getText();
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return null;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return false;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return new Object[0];
  }

  @Override
  public boolean isSoft() {
    return false;
  }
}
