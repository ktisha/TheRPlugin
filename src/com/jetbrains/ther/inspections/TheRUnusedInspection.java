package com.jetbrains.ther.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import com.jetbrains.ther.psi.api.TheRParameter;
import com.jetbrains.ther.psi.api.TheRVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class TheRUnusedInspection extends TheRLocalInspection {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
    return new Visitor(holder);
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Unused inspection";
  }

  //TODO: check not only parameters
  private class Visitor extends TheRVisitor {

    private final ProblemsHolder myProblemHolder;

    public Visitor(@NotNull final ProblemsHolder holder) {
      myProblemHolder = holder;
    }

    @Override
    public void visitParameter(@NotNull TheRParameter o) {
      Query<PsiReference> search = ReferencesSearch.search(o);
      PsiReference first = search.findFirst();
      if (first == null) {
        registerProblem(myProblemHolder, o, "Unused parameter " + o.getText());
      }
    }
  }
}
