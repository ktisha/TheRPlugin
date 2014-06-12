package com.jetbrains.ther.completion;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.jetbrains.ther.lexer.TheRTokenTypes;

public class TheRQuoteHandler extends SimpleTokenSetQuoteHandler {
  public TheRQuoteHandler() {
    super(TheRTokenTypes.STRING_LITERAL);
  }
}
