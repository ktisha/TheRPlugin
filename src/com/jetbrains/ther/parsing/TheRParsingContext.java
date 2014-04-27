package com.jetbrains.ther.parsing;

import com.intellij.lang.PsiBuilder;
import org.jetbrains.annotations.NotNull;

public class TheRParsingContext {
  private final TheRStatementParsing myStmtParser;
  private final TheRExpressionParsing myExpressionParser;
  private final TheRFunctionParsing myFunctionParser;
  private final PsiBuilder myBuilder;

  public TheRParsingContext(@NotNull final PsiBuilder builder) {
    myBuilder = builder;
    myStmtParser = new TheRStatementParsing(this);
    myExpressionParser = new TheRExpressionParsing(this);
    myFunctionParser = new TheRFunctionParsing(this);
  }

  @NotNull
  public TheRStatementParsing getStatementParser() {
    return myStmtParser;
  }

  @NotNull
  public TheRExpressionParsing getExpressionParser() {
    return myExpressionParser;
  }

  @NotNull
  public TheRFunctionParsing getFunctionParser() {
    return myFunctionParser;
  }

  @NotNull
  public PsiBuilder getBuilder() {
    return myBuilder;
  }
}