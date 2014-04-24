package com.jetbrains.ther.psi;

import com.intellij.psi.tree.IElementType;
import com.jetbrains.ther.TheRFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TheRElementType extends IElementType {

  public TheRElementType(@NotNull @NonNls String debugName) {
    super(debugName, TheRFileType.INSTANCE.getLanguage());
  }

  @Override
  public String toString() {
    return "TheR:" + super.toString();
  }
}
