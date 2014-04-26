package com.jetbrains.ther.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class Parsing {
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

}
