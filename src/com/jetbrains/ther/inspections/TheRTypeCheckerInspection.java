package com.jetbrains.ther.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.typing.MatchingException;
import com.jetbrains.ther.typing.TheRTypeChecker;
import com.jetbrains.ther.typing.TheRTypeContext;
import com.jetbrains.ther.typing.types.TheRErrorType;
import com.jetbrains.ther.typing.types.TheRType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TheRTypeCheckerInspection extends TheRLocalInspection {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "R Type Checker";
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
    public void visitCallExpression(@NotNull TheRCallExpression callExpression) {
      PsiReference referenceToFunction = callExpression.getExpression().getReference();
      List<TheRExpression> arguments = callExpression.getArgumentList().getExpressionList();
      checkFunctionCall(callExpression, referenceToFunction, arguments);
      visitExpression(callExpression);
    }

    @Override
    public void visitExpression(@NotNull TheRExpression o) {
      TheRType type = TheRTypeContext.getTypeFromCache(o, false);
      if (type instanceof TheRErrorType) {
        registerProblem(myProblemHolder, o, ((TheRErrorType)type).getErrorMessage(), ProblemHighlightType.GENERIC_ERROR);
      }
    }

    @Override
    public void visitOperatorExpression(@NotNull TheROperatorExpression operatorExpression) {
      TheROperator operator = PsiTreeUtil.getChildOfType(operatorExpression, TheROperator.class);
      if (operator == null) {
        return;
      }
      PsiReference referenceToFunction = operator.getReference();
      List<TheRExpression> arguments = PsiTreeUtil.getChildrenOfTypeAsList(operatorExpression, TheRExpression.class);
      checkFunctionCall(operatorExpression, referenceToFunction, arguments);
    }

    private void checkFunctionCall(PsiElement callSite, PsiReference referenceToFunction, List<TheRExpression> arguments) {
      if (referenceToFunction != null) {
        PsiElement assignmentStatement = referenceToFunction.resolve();
        if (assignmentStatement != null && assignmentStatement instanceof TheRAssignmentStatement) {
          TheRAssignmentStatement assignment = (TheRAssignmentStatement)assignmentStatement;
          TheRPsiElement assignedValue = assignment.getAssignedValue();
          if (assignedValue != null && assignedValue instanceof TheRFunctionExpression) {
            TheRFunctionExpression function = (TheRFunctionExpression)assignedValue;
            try {
              TheRTypeChecker.checkTypes(arguments, function);
            }
            catch (MatchingException e) {
              registerProblem(myProblemHolder, callSite, e.getMessage(), ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
          }
        }
      }
    }
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }
}
