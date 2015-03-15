package com.jetbrains.ther.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.ther.psi.api.TheRReferenceExpression;
import com.jetbrains.ther.psi.api.TheRVisitor;
import com.jetbrains.ther.psi.references.TheRReferenceImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

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
    public void visitReferenceExpression(@NotNull TheRReferenceExpression o) {
      TheRReferenceImpl reference = o.getReference();
      if (reference != null) {
        PsiElement resolve = reference.resolve();
        if (resolve == null) {
          registerProblem(myProblemHolder, o, "Unresolved reference");
        }
      }
    }
  }
}
