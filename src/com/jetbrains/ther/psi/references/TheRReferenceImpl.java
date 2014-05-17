package com.jetbrains.ther.psi.references;

import com.intellij.openapi.module.impl.scopes.LibraryScope;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.ther.interpreter.TheRInterpreterConfigurable;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.psi.stubs.TheRAssignmentNameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
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
      TheRAssignmentStatement assignmentStatement = PsiTreeUtil.getParentOfType(myElement, TheRAssignmentStatement.class);
      while (assignmentStatement != null) {
        final PsiElement assignee = assignmentStatement.getAssignee();
        if (assignee != null && assignee.getText().equals(name)) {
          result.add(new PsiElementResolveResult(assignee));
        }
        assignmentStatement = PsiTreeUtil.getParentOfType(assignmentStatement, TheRAssignmentStatement.class);
      }

      TheRBlock rBlock = PsiTreeUtil.getParentOfType(myElement, TheRBlock.class);
      while (rBlock != null) {
        final TheRAssignmentStatement[] statements = PsiTreeUtil.getChildrenOfType(rBlock, TheRAssignmentStatement.class);
        if (statements != null) {
          for (TheRAssignmentStatement statement : statements) {
            final PsiElement assignee = statement.getAssignee();
            if (assignee != null && assignee.getText().equals(name)) {
              result.add(new PsiElementResolveResult(assignee));
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
          final PsiElement assignee = statement.getAssignee();
          if (assignee != null && assignee.getText().equals(name)) {
            result.add(new PsiElementResolveResult(assignee));
          }
        }
      }
      if (!result.isEmpty())
        return result.toArray(new ResolveResult[result.size()]);
      addFromLibrary(result, name);
    }
    return result.toArray(new ResolveResult[result.size()]);
  }

  private void addFromLibrary(@NotNull final List<ResolveResult> result, @NotNull final String name) {
    final ModifiableModelsProvider modelsProvider = ModifiableModelsProvider.SERVICE.getInstance();
    final LibraryTable.ModifiableModel model = modelsProvider.getLibraryTableModifiableModel(myElement.getProject());
    if (model != null) {
      final Library library = model.getLibraryByName(TheRInterpreterConfigurable.THE_R_LIBRARY);
      if (library != null) {
        final Collection<TheRAssignmentStatement> assignmentStatements = TheRAssignmentNameIndex.find(name, myElement.getProject(),
                                                                                        new LibraryScope(myElement.getProject(), library));
        for (TheRAssignmentStatement statement : assignmentStatements) {
          final PsiFile containingFile = statement.getContainingFile();
          final PsiElement assignee = statement.getAssignee();
          if(assignee == null) continue;
          if (FileUtil.getNameWithoutExtension(containingFile.getName()).equalsIgnoreCase(name)) {
            result.add(0, new PsiElementResolveResult(assignee));
          }
          else
            result.add(new PsiElementResolveResult(assignee));
        }
      }
    }
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
