package com.jetbrains.ther.typing;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.psi.api.*;
import org.jetbrains.annotations.Nullable;

public class TheRTypeProvider {

  //TODO: get type from context

  /**
   * evaluates type of expression
   */
  @Nullable
  public static TheRType getType(PsiElement element)  {
    if (element == null) {
      return TheRType.UNKNOWN;
    }
    if (element instanceof TheRStringLiteralExpression) {
      return TheRCharacterType.INSTANCE;
    }
    if (element instanceof TheRNumericLiteralExpression) {
      return TheRNumericType.INSTANCE;
    }
    //TODO:complete this logic by all the rules
    if (element instanceof TheRReferenceExpression) {
      PsiReference reference = element.getReference();
      if (reference == null) {
        return TheRType.UNKNOWN;
      }
      PsiElement resolve = reference.resolve();
      if (resolve == null) {
        return TheRType.UNKNOWN;
      }
      PsiElement parent = resolve.getParent();
      if (parent != null && parent instanceof TheRAssignmentStatement) {
        TheRAssignmentStatement assignmentStatement = (TheRAssignmentStatement)parent;
        TheRPsiElement assignedValue = assignmentStatement.getAssignedValue();
        if (assignedValue != null) {
          return getType(assignedValue);
        }
      }
    }
    return TheRType.UNKNOWN;
  }

  //TODO: pass parameter list and parse each line only once not for each parameter
  public static TheRType getParamType(TheRParameter parameter) {
    TheRType type = getTypeFromDocString(parameter);
    if (type != null) {
      return type;
    }
    type = guessTypeFromFunctionBody(parameter);
    if (type != null) {
      return type;
    }
    return TheRType.UNKNOWN;
  }


  @Nullable
  private static TheRType guessTypeFromFunctionBody(final TheRParameter parameter) {
    final TheRType[] type = new TheRType[1];
    type[0] = null;
    TheRFunctionExpression function = TheRPsiUtils.getFunction(parameter);
    if (function == null) {
      return null;
    }
    final TheRBlockExpression blockExpression = PsiTreeUtil.getChildOfType(function, TheRBlockExpression.class);
    if (blockExpression == null) {
      return null;
    }
    Query<PsiReference> references = ReferencesSearch.search(parameter);
    references.forEach(new Processor<PsiReference>() {
      @Override
      public boolean process(PsiReference reference) {
        PsiElement element = reference.getElement();
        PsiElement parent = element.getParent();
        //TODO: check operations more strict
        //TODO: check control flow analysis
        if (parent instanceof TheRBinaryExpression) {
          if (PsiTreeUtil.isAncestor(blockExpression, element, false)) {
            type[0] = TheRNumericType.INSTANCE;
            return false;
          }
        }
        return true;
      }
    });
    return type[0];
  }

  //TODO:rewrite this normally
  @Nullable
  public static TheRType findTypeByName(String typeName) {
    if (typeName.equals("numeric")) {
      return  TheRNumericType.INSTANCE;
    }
    if (typeName.equals("character")) {
      return  TheRCharacterType.INSTANCE;
    }
    return null;
  }

  /**
   *
   * @param parameter parameter to get type from docstring
   * @return null if parameter type isn't specified in docstring or type otherwise
   */
  @Nullable
  private static TheRType getTypeFromDocString(TheRParameter parameter) {
    TheRAssignmentStatement assignmentStatement = TheRPsiUtils.getAssignmentStatement(parameter);
    if (assignmentStatement == null) {
      return null;
    }
    PsiElement prevSibling = assignmentStatement.getPrevSibling();
    while (prevSibling != null && !(prevSibling instanceof TheRPsiElement)) {
      if (prevSibling instanceof PsiComment) {
        TheRType type = DocStringUtil.parse(parameter, prevSibling.getText());
        if (type != null) {
          return type;
        }
      }
      prevSibling = prevSibling.getPrevSibling();
    }
    return null;
  }
}
