package com.jetbrains.ther.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Parsing {
  protected static final String EXPRESSION_EXPECTED = "Expression expected";

  private final TheRParsingContext myContext;
  protected final PsiBuilder myBuilder;

  public Parsing(@NotNull final TheRParsingContext context) {
    myContext = context;
    myBuilder = context.getBuilder();
  }

  @NotNull
  public TheRParsingContext getParsingContext() {
    return myContext;
  }

  @NotNull
  public TheRExpressionParsing getExpressionParser() {
    return getParsingContext().getExpressionParser();
  }

  @NotNull
  public TheRFunctionParsing getFunctionParser() {
    return getParsingContext().getFunctionParser();
  }

  protected static void buildTokenElement(@NotNull final IElementType type, @NotNull final PsiBuilder builder) {
    final PsiBuilder.Marker marker = builder.mark();
    builder.advanceLexer();
    marker.done(type);
  }

  protected boolean matchToken(@NotNull final IElementType tokenType) {
    if (myBuilder.getTokenType() == tokenType) {
      myBuilder.advanceLexer();
      return true;
    }
    return false;
  }
  protected boolean checkMatches(final IElementType token, final String message) {
    if (myBuilder.getTokenType() == token) {
      myBuilder.advanceLexer();
      return true;
    }
    myBuilder.error(message);
    return false;
  }
  protected boolean atToken(@Nullable final IElementType tokenType) {
    return myBuilder.getTokenType() == tokenType;
  }

  protected void skipNewLines() {
    while (myBuilder.getTokenType() == TheRTokenTypes.LINE_BREAK) {
      myBuilder.advanceLexer();
    }
  }

  protected void checkSemicolon() {
    while (myBuilder.getTokenType() == TheRTokenTypes.SEMICOLON) {
      myBuilder.advanceLexer();
      while (myBuilder.getTokenType() == TheRTokenTypes.LINE_BREAK) {
        myBuilder.advanceLexer();
      }
    }
    while (myBuilder.getTokenType() == TheRTokenTypes.LINE_BREAK) {
      myBuilder.advanceLexer();
    }
  }

  protected void advanceAndSkipNewLines() {
    myBuilder.advanceLexer();
    skipNewLines();
  }

}
