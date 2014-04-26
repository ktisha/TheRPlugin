package com.jetbrains.ther.psi.api;

import com.intellij.psi.NavigatablePsiElement;

public interface TheRElement extends NavigatablePsiElement {
  /**
   * An empty array to return cheaply without allocating it anew.
   */
  TheRElement[] EMPTY_ARRAY = new TheRElement[0];

}
