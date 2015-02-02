package com.jetbrains.ther.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.ther.parsing.TheRParserDefinition;

public class TheRLexer extends MergingLexerAdapter {
  private static final TokenSet TOKENS_TO_MERGE = TokenSet.create(TheRParserDefinition.SPACE);

  public TheRLexer() {
    super(new FlexAdapter(new _TheRLexer((java.io.Reader)null)), TOKENS_TO_MERGE);
  }

}
