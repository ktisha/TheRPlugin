package com.jetbrains.ther.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.psi.api.TheRVisitor;
import com.jetbrains.ther.typing.TheRTypeContext;
import com.jetbrains.ther.typing.types.TheRErrorType;
import com.jetbrains.ther.typing.types.TheRType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class TheRErrorTypeInspection extends TheRLocalInspection {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "R error type inspection";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
    return new Visitor(holder);
  }

  private class Visitor extends TheRVisitor {
    private ProblemsHolder myHolder;

    public Visitor(ProblemsHolder holder) {
      myHolder = holder;
    }

    @Override
    public void visitExpression(@NotNull TheRExpression o) {
      TheRType type = TheRTypeContext.getTypeFromCache(o, false);
      if (type instanceof TheRErrorType) {
        registerProblem(myHolder, o, ((TheRErrorType)type).getErrorMessage(), ProblemHighlightType.GENERIC_ERROR);
      }
    }
  }
}
