package com.jetbrains.ther.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.typing.TheRTypeChecker;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TheRTypeCheckerInspection extends LocalInspectionTool {
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

  class Visitor extends TheRVisitor {
    private final ProblemsHolder myProblemHolder;

    public Visitor(ProblemsHolder holder) {
      myProblemHolder = holder;
    }

    @Override
    public void visitCallExpression(@NotNull TheRCallExpression o) {
      PsiReference referenceToFunction = o.getExpression().getReference();
      if (referenceToFunction != null) {
        PsiElement resolve = referenceToFunction.resolve();
        if (resolve instanceof TheRReferenceExpression) {
          PsiElement assignmentStatement = resolve.getParent();
          if (assignmentStatement != null && assignmentStatement instanceof TheRAssignmentStatement) {
            TheRAssignmentStatement assignment = (TheRAssignmentStatement) assignmentStatement;
            TheRPsiElement assignedValue = assignment.getAssignedValue();
            if (assignedValue != null && assignedValue instanceof  TheRFunctionExpression) {
              TheRFunctionExpression function = (TheRFunctionExpression)assignedValue;
              List<TheRExpression> arguments = o.getArgumentList().getExpressionList();
              List<TheRParameter> parameters = function.getParameterList().getParameterList();

              String errorMessage = TheRTypeChecker.matchTypes(parameters, arguments);
              if (errorMessage != null) {
                registerProblem(myProblemHolder, o, errorMessage);
              }
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

  private void registerProblem(ProblemsHolder holder, PsiElement element, String message) {
    if (holder!= null) {
      holder.registerProblem(element, message);
    }
  }
}
