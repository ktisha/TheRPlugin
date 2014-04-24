package com.jetbrains.ther.lexer;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;

public class TheRLexer extends FlexAdapter {
  public TheRLexer() {
    super(new _TheRLexer((Reader)null));
  }

  public _TheRLexer getFlex() {
    return (_TheRLexer)super.getFlex();
  }
}
