package com.jetbrains.ther.psi;

import com.intellij.lang.Language;
import com.intellij.psi.tree.IFileElementType;
import com.jetbrains.ther.TheRLanguage;

public class TheRFileElementType extends IFileElementType {
  public TheRFileElementType() {
    super(Language.findInstance(TheRLanguage.class));
  }
}
