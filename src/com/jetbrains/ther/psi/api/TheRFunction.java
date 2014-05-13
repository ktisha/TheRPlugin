package com.jetbrains.ther.psi.api;

import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

public interface TheRFunction extends TheRElement, PsiNamedElement {
  @NotNull
  TheRParameterList getParameterList();

}
