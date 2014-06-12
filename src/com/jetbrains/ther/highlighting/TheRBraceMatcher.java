package com.jetbrains.ther.highlighting;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRBraceMatcher implements PairedBraceMatcher {
  private final BracePair[] PAIRS = new BracePair[]{
    new BracePair(TheRTokenTypes.LPAR, TheRTokenTypes.RPAR, false),
      new BracePair(TheRTokenTypes.LBRACKET, TheRTokenTypes.RBRACKET, false),
      new BracePair(TheRTokenTypes.LDBRACKET, TheRTokenTypes.RDBRACKET, false),
      new BracePair(TheRTokenTypes.LBRACE, TheRTokenTypes.RBRACE, false)};

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
