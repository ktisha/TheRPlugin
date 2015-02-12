package com.jetbrains.ther.psi.api;

import com.intellij.psi.NavigatablePsiElement;

public interface TheRPsiElement extends NavigatablePsiElement {
  /**
   * An empty array to return cheaply without allocating it anew.
   */
  TheRPsiElement[] EMPTY_ARRAY = new TheRPsiElement[0];

}
