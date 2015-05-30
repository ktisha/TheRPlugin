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
import com.jetbrains.ther.typing.TheRTypeProvider;
import com.jetbrains.ther.typing.types.TheRErrorType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

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
      TheRTypeProvider.getType(callExpression);
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
      TheRTypeProvider.getType(operatorExpression);
    }

    @Override
    public void visitAtExpression(@NotNull TheRAtExpression o) {
      TheRTypeProvider.getType(o);
    }

    @Override
    public void visitMemberExpression(@NotNull TheRMemberExpression o) {
      TheRTypeProvider.getType(o);
    }

    @Override
    public void visitSubscriptionExpression(@NotNull TheRSubscriptionExpression o) {
      TheRTypeProvider.getType(o);
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

  @Override
  public void inspectionFinished(@NotNull LocalInspectionToolSession session, @NotNull ProblemsHolder problemsHolder) {
    Map<TheRPsiElement, TheRErrorType> errors = TheRTypeContext.getExpressionsWithError(problemsHolder.getProject());
    for (Map.Entry<TheRPsiElement, TheRErrorType> error : errors.entrySet()) {
      registerProblem(problemsHolder, error.getKey(), error.getValue().getErrorMessage(), ProblemHighlightType.GENERIC_ERROR);
    }
  }
}
