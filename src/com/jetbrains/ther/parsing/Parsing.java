package com.jetbrains.ther.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Parsing {
  protected static final String EXPRESSION_EXPECTED = "Expression expected";
  protected static final String IDENTIFIER_EXPECTED = "Identifier expected";

  private final TheRParsingContext myContext;
  protected final PsiBuilder myBuilder;
  private static final Logger LOG = Logger.getInstance(Parsing.class.getName());

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
  public TheRStatementParsing getStatementParser() {
    return getParsingContext().getStatementParser();
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

}
