package com.jetbrains.ther.highlighting;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.ther.parsing.TheRElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRBraceMatcher implements PairedBraceMatcher {
  private final BracePair[] PAIRS = new BracePair[]{
    new BracePair(TheRElementTypes.THE_R_LPAR, TheRElementTypes.THE_R_RPAR, false),
      new BracePair(TheRElementTypes.THE_R_LBRACKET, TheRElementTypes.THE_R_RBRACKET, false),
      new BracePair(TheRElementTypes.THE_R_LDBRACKET, TheRElementTypes.THE_R_RDBRACKET, false),
      new BracePair(TheRElementTypes.THE_R_LBRACE, TheRElementTypes.THE_R_RBRACE, false)};

  @Override
  public BracePair[] getPairs() {
    return PAIRS;
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
    return true;
  }

  @Override
  public int getCodeConstructStart(final PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}
