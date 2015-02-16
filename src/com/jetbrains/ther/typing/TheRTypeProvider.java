package com.jetbrains.ther.typing;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
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
    TheRAssignmentStatement assignmentStatement = TheRPsiUtils.getAssignmentStatement(parameter);
    if (assignmentStatement == null) {
      return TheRType.UNKNOWN;
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
    return TheRType.UNKNOWN;
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
}
