package com.jetbrains.ther.completion;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.jetbrains.ther.lexer.TheRTokenTypes;

public class TheRQuoteHandler extends SimpleTokenSetQuoteHandler {
  public TheRQuoteHandler() {
    super(TheRTokenTypes.STRING_LITERAL);
  }

  @Override
  public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
    final Document document = editor.getDocument();
    int lineEndOffset = document.getLineEndOffset(document.getLineNumber(offset));
    if (offset < lineEndOffset) {
      final CharSequence charSequence = document.getCharsSequence();
      final char openQuote = charSequence.charAt(offset);
      final int nextCharOffset = offset + 1;
      if (nextCharOffset < lineEndOffset && charSequence.charAt(nextCharOffset) == openQuote) {
        return true;
      }
      for (int i = nextCharOffset + 1; i < lineEndOffset; i++) {
        if (charSequence.charAt(i) == openQuote) {
          return false;
        }
      }
    }
    return true;
  }

}
