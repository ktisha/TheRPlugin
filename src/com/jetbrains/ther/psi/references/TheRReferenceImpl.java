package com.jetbrains.ther.psi.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.impl.scopes.LibraryScope;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.ther.TheRElementGenerator;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.interpreter.TheRInterpreterConfigurable;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.psi.stubs.TheRAssignmentNameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TheRReferenceImpl implements PsiPolyVariantReference {
  protected final TheRReferenceExpression myElement;

  public TheRReferenceImpl(TheRReferenceExpression element) {
    myElement = element;
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final List<ResolveResult> result = new ArrayList<ResolveResult>();

    if (TheRPsiUtils.isNamedArgument(myElement)) {
      TheRResolver.resolveNameArgument(myElement, myElement.getName(), result);
      return result.toArray(new ResolveResult[result.size()]);
    }

    final String namespace = myElement.getNamespace();
    final String name = myElement.getName();
    if (name == null) return ResolveResult.EMPTY_ARRAY;
    if (namespace != null) {
      TheRResolver.resolveWithNamespace(myElement.getProject(), name, namespace, result);
    }

    TheRResolver.resolveFunction(myElement, name, result);
    if (!result.isEmpty()) {
      return result.toArray(new ResolveResult[result.size()]);
    }

    TheRResolver.resolveWithoutNamespaceInFile(myElement, name, result);
    if (!result.isEmpty()) {
      return result.toArray(new ResolveResult[result.size()]);
    }
    TheRResolver.addFromSkeletonsAndRLibrary(myElement, result, name);
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
    final ASTNode oldNameIdentifier = getElement().getNode().findChildByType(TheRElementTypes.THE_R_IDENTIFIER);
    if (oldNameIdentifier != null) {
      final PsiFile dummyFile = TheRElementGenerator.createDummyFile(newElementName, false, getElement().getProject());
      ASTNode identifier = dummyFile.getNode().getFirstChildNode().findChildByType(TheRElementTypes.THE_R_IDENTIFIER);
      if (identifier != null) {
        getElement().getNode().replaceChild(oldNameIdentifier, identifier);
      }
    }
    return getElement();
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    //TODO: check some conditions
    return resolve() == element;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    List<LookupElement> result = new ArrayList<LookupElement>();
    final String name = myElement.getName();
    if (myElement.getParent() instanceof TheRReferenceExpression) return ResolveResult.EMPTY_ARRAY;
    if (name == null) return ResolveResult.EMPTY_ARRAY;

    TheRBlockExpression rBlock = PsiTreeUtil.getParentOfType(myElement, TheRBlockExpression.class);
    while (rBlock != null) {
      final TheRAssignmentStatement[] statements = PsiTreeUtil.getChildrenOfType(rBlock, TheRAssignmentStatement.class);
      if (statements != null) {
        for (TheRAssignmentStatement statement : statements) {
          final PsiElement assignee = statement.getAssignee();
          if (assignee != null) {
            result.add(LookupElementBuilder.create(assignee.getText()));
          }
        }
      }
      rBlock = PsiTreeUtil.getParentOfType(rBlock, TheRBlockExpression.class);
    }
    final TheRFunctionExpression rFunction = PsiTreeUtil.getParentOfType(myElement, TheRFunctionExpression.class);
    if (rFunction != null) {
      final TheRParameterList list = rFunction.getParameterList();
      for (TheRParameter parameter : list.getParameterList()) {
        result.add(LookupElementBuilder.create(parameter));
      }
    }
    final PsiFile file = myElement.getContainingFile();
    final TheRAssignmentStatement[] statements = PsiTreeUtil.getChildrenOfType(file, TheRAssignmentStatement.class);
    if (statements != null) {
      for (TheRAssignmentStatement statement : statements) {
        final PsiElement assignee = statement.getAssignee();
        if (assignee != null) {
          result.add(LookupElementBuilder.create(assignee.getText()));
        }
      }
    }
    addVariantsFromSkeletons(result);
    return result.toArray();
  }

  private void addVariantsFromSkeletons(@NotNull final List<LookupElement> result) {
    final ModifiableModelsProvider modelsProvider = ModifiableModelsProvider.SERVICE.getInstance();
    final LibraryTable.ModifiableModel model = modelsProvider.getLibraryTableModifiableModel(myElement.getProject());
    if (model != null) {
      final Library library = model.getLibraryByName(TheRInterpreterConfigurable.THE_R_SKELETONS);
      if (library != null) {
        final Collection<String> assignmentStatements = TheRAssignmentNameIndex.allKeys(myElement.getProject());
        for (String statement : assignmentStatements) {
          final Collection<TheRAssignmentStatement> statements =
            TheRAssignmentNameIndex.find(statement, myElement.getProject(), new LibraryScope(myElement.getProject(), library));
          for (TheRAssignmentStatement assignmentStatement : statements) {
            final PsiDirectory directory = assignmentStatement.getContainingFile().getParent();
            assert directory != null;
            if (directory.getName().equals("base")) {
              result.add(LookupElementBuilder.create(assignmentStatement));
            }
            else {
              result.add(LookupElementBuilder.create(assignmentStatement, directory.getName() + "::" + assignmentStatement.getName()));
            }
          }
        }
      }
    }
  }

  @Override
  public boolean isSoft() {
    return false;
  }
}
