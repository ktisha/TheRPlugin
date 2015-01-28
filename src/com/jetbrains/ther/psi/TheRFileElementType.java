package com.jetbrains.ther.psi;

import com.intellij.psi.tree.IStubFileElementType;
import com.jetbrains.ther.TheRLanguage;

public class TheRFileElementType extends IStubFileElementType {
  public TheRFileElementType() {
    super(TheRLanguage.getInstance());
  }

  @Override
  public int getStubVersion() {
    return 1;
  }
}
