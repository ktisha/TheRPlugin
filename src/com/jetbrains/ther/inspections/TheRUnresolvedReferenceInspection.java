package com.jetbrains.ther.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.psi.references.TheRReferenceImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TheRUnresolvedReferenceInspection extends TheRLocalInspection {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Unresolved reference inspection";
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
    return new Visitor(holder);
  }

  private class Visitor extends TheRVisitor {

    private final ProblemsHolder myProblemHolder;

    public Visitor(ProblemsHolder holder) {
      myProblemHolder = holder;
    }

    @Override
    public void visitReferenceExpression(@NotNull TheRReferenceExpression element) {
      PsiElement sibling = element.getNextSibling();
      if (sibling != null && sibling.getNode().getElementType() == TheRElementTypes.THE_R_DOUBLECOLON) {
        return;
      }

      if (TheRPsiUtils.isNamedArgument(element)) {
        return;
      }

      TheRCallExpression callExpression = PsiTreeUtil.getParentOfType(element, TheRCallExpression.class);
      if (callExpression != null) {
        TheRFunctionExpression function = TheRPsiUtils.getFunction(callExpression);
        if (function != null) {
          List<TheRParameter> list = function.getParameterList().getParameterList();
          if (TheRPsiUtils.containsTripleDot(list)) {
            return;
          }
        }
      }

      TheRReferenceImpl reference = element.getReference();
      if (reference != null) {
        PsiElement resolve = reference.resolve();
        if (resolve == null) {
          registerProblem(myProblemHolder, element, "Unresolved reference", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
      }
    }
  }
}
