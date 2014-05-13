package com.jetbrains.ther.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.ther.psi.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheRReferenceImpl implements PsiReference, PsiPolyVariantReference {
  protected final TheRElement myElement;

  public TheRReferenceImpl(TheRElement element) {
    myElement = element;
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final List<ResolveResult> result = new ArrayList<ResolveResult>();
    final String name = myElement.getText();
    if (name != null) {
      TheRBlock rBlock = PsiTreeUtil.getParentOfType(myElement, TheRBlock.class);
      while (rBlock != null) {
        final TheRAssignmentStatement[] statements = PsiTreeUtil.getChildrenOfType(rBlock, TheRAssignmentStatement.class);
        if (statements != null) {
          for (TheRAssignmentStatement statement : statements) {
            final PsiElement child = statement.getFirstChild();
            if (child.getText().equals(name)) {
              result.add(new PsiElementResolveResult(statement));
            }
          }
        }
        rBlock = PsiTreeUtil.getParentOfType(rBlock, TheRBlock.class);
      }
      final TheRFunction rFunction = PsiTreeUtil.getParentOfType(myElement, TheRFunction.class);
      if (rFunction != null) {
        final TheRParameterList list = rFunction.getParameterList();
        for (TheRParameter parameter : list.getParameters()) {
          if (name.equals(parameter.getName())) {
            result.add(new PsiElementResolveResult(parameter));
          }
        }
      }
      final PsiFile file = myElement.getContainingFile();
      final TheRAssignmentStatement[] statements = PsiTreeUtil.getChildrenOfType(file, TheRAssignmentStatement.class);
      if (statements != null) {
        for (TheRAssignmentStatement statement : statements) {
          final PsiElement child = statement.getFirstChild();
          if (child.getText().equals(name)) {
            result.add(new PsiElementResolveResult(statement));
          }
        }
      }
      if (!result.isEmpty())
        return result.toArray(new ResolveResult[result.size()]);

      //final Collection<TheRAssignmentStatement> assignmentStatements = TheRAssignmentNameIndex.find(name, myElement.getProject());
      //for (TheRAssignmentStatement statement : assignmentStatements) {
      //  result.add(new PsiElementResolveResult(statement));
      //}
    }
    return result.toArray(new ResolveResult[result.size()]);
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
    final ResolveResult[] results = multiResolve(false);
    return results.length >= 1 ? results[0].getElement() : null;

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
