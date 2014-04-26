package com.jetbrains.ther.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import org.jetbrains.annotations.NotNull;

public class TheRExpressionParsing extends Parsing {
  private static final Logger LOG = Logger.getInstance(TheRExpressionParsing.class.getName());

  public TheRExpressionParsing(@NotNull final TheRParsingContext context) {
    super(context);
  }

  public boolean parsePrimaryExpression() {
    final IElementType firstToken = myBuilder.getTokenType();
    if (firstToken == TheRTokenTypes.NUMERIC_LITERAL) {
      buildTokenElement(TheRElementTypes.INTEGER_LITERAL_EXPRESSION, myBuilder);
      return true;
    }
    else if (firstToken == TheRTokenTypes.NUMERIC_LITERAL) {
      buildTokenElement(TheRElementTypes.FLOAT_LITERAL_EXPRESSION, myBuilder);
      return true;
    }
    else if (firstToken == TheRTokenTypes.COMPLEX_LITERAL) {
      buildTokenElement(TheRElementTypes.IMAGINARY_LITERAL_EXPRESSION, myBuilder);
      return true;
    }
    else if (firstToken == TheRTokenTypes.STRING_LITERAL) {
      buildTokenElement(TheRElementTypes.STRING_LITERAL_EXPRESSION, myBuilder);
      return true;
    }
    else if (firstToken == TheRTokenTypes.IDENTIFIER) {
      buildTokenElement(TheRElementTypes.REFERENCE_EXPRESSION, myBuilder);
      return true;
    }

    else {
      myBuilder.advanceLexer();
    }
    return false;
  }

  public boolean parseExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseANDExpression()) {
      expr.drop();
      return false;
    }
    while (myBuilder.getTokenType() == TheRTokenTypes.OR) {
      myBuilder.advanceLexer();
      if (!parseANDExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  private boolean parseANDExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseNOTExpression()) {
      expr.drop();
      return false;
    }
    while (myBuilder.getTokenType() == TheRTokenTypes.AND) {
      myBuilder.advanceLexer();
      if (!parseNOTExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  private boolean parseNOTExpression() {
    if (myBuilder.getTokenType() == TheRTokenTypes.NOT) {
      final PsiBuilder.Marker expr = myBuilder.mark();
      myBuilder.advanceLexer();
      if (!parseNOTExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.PREFIX_EXPRESSION);
      return true;
    }
    else {
      return parseComparisonExpression();
    }
  }

  private boolean parseComparisonExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseAdditiveExpression()) {
      myBuilder.error(EXPRESSION_EXPECTED);
      expr.drop();
      return false;
    }
    while (TheRTokenTypes.COMPARISON_OPERATIONS.contains(myBuilder.getTokenType())) {
      myBuilder.advanceLexer();

      if (!parseAdditiveExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  private boolean parseAdditiveExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseMultiplicativeExpression()) {
      expr.drop();
      return false;
    }
    while (TheRTokenTypes.ADDITIVE_OPERATIONS.contains(myBuilder.getTokenType())) {
      myBuilder.advanceLexer();
      if (!parseMultiplicativeExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  private boolean parseMultiplicativeExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseSliceExpression()) {
      expr.drop();
      return false;
    }

    while (TheRTokenTypes.MULTIPLICATIVE_OPERATIONS.contains(myBuilder.getTokenType())) {
      myBuilder.advanceLexer();
      if (!parseSliceExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  protected boolean parseSliceExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseUnaryExpression()) {
      expr.drop();
      return false;
    }

    while (TheRTokenTypes.COLON == myBuilder.getTokenType()) {
      myBuilder.advanceLexer();
      if (!parseUnaryExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.SLICE_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  protected boolean parseUnaryExpression() {
    final IElementType tokenType = myBuilder.getTokenType();
    if (TheRTokenTypes.UNARY_OPERATIONS.contains(tokenType)) {
      final PsiBuilder.Marker expr = myBuilder.mark();
      myBuilder.advanceLexer();
      if (!parseUnaryExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.PREFIX_EXPRESSION);
      return true;
    }
    else {
      return parsePowerExpression();
    }
  }

  private boolean parsePowerExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseMemberExpression()) {
      expr.drop();
      return false;
    }

    if (myBuilder.getTokenType() == TheRTokenTypes.EXP) {
      myBuilder.advanceLexer();
      if (!parseUnaryExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
    }
    else {
      expr.drop();
    }

    return true;
  }

  public boolean parseMemberExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parsePrimaryExpression()) {
      expr.drop();
      return false;
    }

    while (true) {
      final IElementType tokenType = myBuilder.getTokenType();
      if (tokenType == TheRTokenTypes.DOT) {
        myBuilder.advanceLexer();
        expr.done(TheRElementTypes.REFERENCE_EXPRESSION);
        expr = expr.precede();
      }
      else if (tokenType == TheRTokenTypes.LPAR) {
        parseArgumentList();
        expr.done(TheRElementTypes.CALL_EXPRESSION);
        expr = expr.precede();
      }
      else {
        expr.drop();
        break;
      }
    }
    return true;
  }

  public void parseArgumentList() {
    LOG.assertTrue(myBuilder.getTokenType() == TheRTokenTypes.LPAR);
    final PsiBuilder.Marker arglist = myBuilder.mark();
    myBuilder.advanceLexer();
    PsiBuilder.Marker genexpr = myBuilder.mark();
    int argNumber = 0;
    while (myBuilder.getTokenType() != TheRTokenTypes.RPAR) {
      argNumber++;
      if (argNumber > 1) {
        if (matchToken(TheRTokenTypes.COMMA)) {
          if (atToken(TheRTokenTypes.RPAR)) {
            break;
          }
        }
        else {
          myBuilder.error("',' or ')' expected");
          break;
        }
      }
      if (myBuilder.getTokenType() == TheRTokenTypes.IDENTIFIER) {
        final PsiBuilder.Marker keywordArgMarker = myBuilder.mark();
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() == TheRTokenTypes.EQ) {
          myBuilder.advanceLexer();
          if (!parseExpression()) {
            myBuilder.error(EXPRESSION_EXPECTED);
          }
          keywordArgMarker.done(TheRElementTypes.KEYWORD_ARGUMENT_EXPRESSION);
          continue;
        }
        keywordArgMarker.rollbackTo();
      }
      if (!parseExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
        break;
      }
    }


    if (genexpr != null) {
      genexpr.drop();
    }
    checkMatches(TheRTokenTypes.RPAR, "')' expected");
    arglist.done(TheRElementTypes.ARGUMENT_LIST);
  }


}