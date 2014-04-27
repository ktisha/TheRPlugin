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
    else if (firstToken == TheRTokenTypes.NA_KEYWORD) {
      buildTokenElement(TheRElementTypes.REFERENCE_EXPRESSION, myBuilder);
      return true;
    }
    else if (TheRTokenTypes.SPECIAL_CONSTANTS.contains(firstToken)) {
      buildTokenElement(TheRElementTypes.REFERENCE_EXPRESSION, myBuilder);
      return true;
    }
    else if (firstToken == TheRTokenTypes.INFIX_OP) {
      buildTokenElement(TheRElementTypes.OPERATOR_EXPRESSION, myBuilder);
      return true;
    }
    else if (firstToken == TheRTokenTypes.TICK) {
      parseReprExpression();
      return true;
    }
    else if (firstToken == TheRTokenTypes.LPAR) {
      parseParenthesizedExpression();
      return true;
    }
    else {
      myBuilder.advanceLexer();
    }
    return false;
  }

  public boolean parseExpression() {
    if (myBuilder.getTokenType() == TheRTokenTypes.HELP) {
      final PsiBuilder.Marker mark = myBuilder.mark();
      myBuilder.advanceLexer();
      if (myBuilder.getTokenType() == TheRTokenTypes.HELP) {
        myBuilder.advanceLexer();
      }
      if (TheRTokenTypes.KEYWORDS.contains(myBuilder.getTokenType())) {
        myBuilder.advanceLexer();
        mark.done(TheRElementTypes.HELP_EXPRESSION);
        return true;
      }
      else if (parseModelFormulaeExpression()) {
        mark.done(TheRElementTypes.HELP_EXPRESSION);
        return true;
      }
      else {
        myBuilder.error(EXPRESSION_EXPECTED);
        mark.drop();
        return false;
      }
    }
    return parseModelFormulaeExpression();
  }

  public boolean parseModelFormulaeExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseOrExpression()) {
      expr.drop();
      return false;
    }
    while (TheRTokenTypes.TILDE == myBuilder.getTokenType()) {
      myBuilder.advanceLexer();
      if (!parseOrExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  public boolean parseOrExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseANDExpression()) {
      expr.drop();
      return false;
    }
    while (TheRTokenTypes.OR_OPERATIONS.contains(myBuilder.getTokenType())) {
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
    if (!parseUserDefinedExpression()) {
      expr.drop();
      return false;
    }
    while (TheRTokenTypes.AND_OPERATIONS.contains(myBuilder.getTokenType())) {
      myBuilder.advanceLexer();
      if (!parseUserDefinedExpression()) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  private boolean parseUserDefinedExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseNOTExpression()) {
      expr.drop();
      return false;
    }
    while (TheRTokenTypes.INFIX_OP == myBuilder.getTokenType()) {
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

    if (TheRTokenTypes.POWER_OPERATIONS.contains(myBuilder.getTokenType())) {
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
      if (tokenType == TheRTokenTypes.LIST_SUBSET) {
        myBuilder.advanceLexer();
        if (!parseStringOrIdentifier()) {
          myBuilder.error("Expected string or identifier");
        }
        expr.done(TheRElementTypes.REFERENCE_EXPRESSION);
        expr = expr.precede();
      }
      else if (tokenType == TheRTokenTypes.LPAR) {
        parseArgumentList();
        expr.done(TheRElementTypes.CALL_EXPRESSION);
        expr = expr.precede();
      }
      else if (TheRTokenTypes.NAMESPACE_ACCESS.contains(tokenType)) {
        myBuilder.advanceLexer();
        parseExpression();
        expr.done(TheRElementTypes.REFERENCE_EXPRESSION);
        expr = expr.precede();
      }
      else if (TheRTokenTypes.OPEN_BRACKETS.contains(tokenType)) {
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() == TheRTokenTypes.COMMA) {
          myBuilder.advanceLexer();
          PsiBuilder.Marker marker = myBuilder.mark();
          marker.done(TheRElementTypes.EMPTY_EXPRESSION);
        }
        final IElementType CLOSE_BRACKET = TheRTokenTypes.BRACKER_PAIRS.get(tokenType);
        while (myBuilder.getTokenType() != CLOSE_BRACKET && !myBuilder.eof()) {
          if (parseExpression()) {
            if (myBuilder.getTokenType() == TheRTokenTypes.COMMA) {
              myBuilder.advanceLexer();
            }
            else {
              break;
            }
          }
          else {
            break;
          }
        }

        checkMatches(CLOSE_BRACKET, "Closing ] or ]] expected");
        expr.done(TheRElementTypes.SUBSCRIPTION_EXPRESSION);
        expr = expr.precede();
      }
      else {
        expr.drop();
        break;
      }
    }
    return true;
  }

  private boolean parseStringOrIdentifier() {
    if (myBuilder.getTokenType() == TheRTokenTypes.STRING_LITERAL) {
      buildTokenElement(TheRElementTypes.STRING_LITERAL_EXPRESSION, myBuilder);
      return true;
    }
    else if (myBuilder.getTokenType() == TheRTokenTypes.IDENTIFIER) {
      buildTokenElement(TheRElementTypes.REFERENCE_EXPRESSION, myBuilder);
      return true;
    }
    return false;
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
      if (myBuilder.getTokenType() == TheRTokenTypes.IDENTIFIER || myBuilder.getTokenType() == TheRTokenTypes.STRING_LITERAL) {
        final PsiBuilder.Marker keywordArgMarker = myBuilder.mark();
        parseExpression();

        if (TheRTokenTypes.ASSIGNMENTS.contains(myBuilder.getTokenType())) {
          myBuilder.advanceLexer();
          if (!parseExpression()) {
            myBuilder.error(EXPRESSION_EXPECTED);
          }
          keywordArgMarker.done(TheRElementTypes.KEYWORD_ARGUMENT_EXPRESSION);
          continue;
        }
        keywordArgMarker.rollbackTo();
      }
      if (myBuilder.getTokenType() == TheRTokenTypes.LBRACE) {
        getStatementParser().parseBlock();
        continue;
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

  private void parseReprExpression() {
    LOG.assertTrue(myBuilder.getTokenType() == TheRTokenTypes.TICK);
    final PsiBuilder.Marker expr = myBuilder.mark();
    myBuilder.advanceLexer();
    parseExpression();
    checkMatches(TheRTokenTypes.TICK, "'`' (backtick) expected");
    expr.done(TheRElementTypes.REPR_EXPRESSION);
  }

  private void parseParenthesizedExpression() {
    LOG.assertTrue(myBuilder.getTokenType() == TheRTokenTypes.LPAR);
    final PsiBuilder.Marker expr = myBuilder.mark();
    myBuilder.advanceLexer();
    parseExpression();
    checkMatches(TheRTokenTypes.RPAR, ") expected");
    expr.done(TheRElementTypes.PARENTHESIZED_EXPRESSION);
  }

}