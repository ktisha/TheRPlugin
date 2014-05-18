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

  public void parseExpressionStatement(boolean skipLineBreak) {
    skipNewLines();

    final IElementType firstToken = myBuilder.getTokenType();
    if (firstToken == null) return;

    if (firstToken == TheRTokenTypes.IF_KEYWORD) {
      parseIfExpression();
    }
    else if (firstToken == TheRTokenTypes.WHILE_KEYWORD) {
      parseWhileExpression();
    }
    else if (firstToken == TheRTokenTypes.FOR_KEYWORD) {
      parseForExpression();
    }
    else if (firstToken == TheRTokenTypes.REPEAT_KEYWORD) {
      parseRepeatExpression();
    }
    else if (firstToken == TheRTokenTypes.BREAK_KEYWORD) {
      parseBreakExpression();
    }
    else if (firstToken == TheRTokenTypes.NEXT_KEYWORD) {
      parseNextExpression();
    }
    else if (firstToken == TheRTokenTypes.LBRACE) {
      parseBlockExpression();
    }
    else if (firstToken == TheRTokenTypes.FUNCTION_KEYWORD) {
      getFunctionParser().parseFunctionDeclaration();
    }
    else if (firstToken == TheRTokenTypes.HELP) {
      parseHelpExpression();
    }
    else {
      parseAssignmentExpression(skipLineBreak);
    }
  }

  private void parseIfExpression() {
    final PsiBuilder.Marker ifExpression = myBuilder.mark();
    parseConditionExpression();
    parseExpressionStatement(false);
    skipNewLines();
    if (myBuilder.getTokenType() == TheRTokenTypes.ELSE_KEYWORD) {
      myBuilder.advanceLexer();
      parseExpressionStatement(false);
    }
    checkSemicolon();
    ifExpression.done(TheRElementTypes.IF_STATEMENT);
  }

  private void parseWhileExpression() {
    final PsiBuilder.Marker whileExpression = myBuilder.mark();
    parseConditionExpression();
    parseExpressionStatement(false);
    checkSemicolon();
    whileExpression.done(TheRElementTypes.WHILE_STATEMENT);
  }

  private void parseConditionExpression() {
    advanceAndSkipNewLines();
    checkMatches(TheRTokenTypes.LPAR, "( expected");
    parseExpressionStatement(true);
    skipNewLines();
    checkMatches(TheRTokenTypes.RPAR, ") expected");
    skipNewLines();
  }

  private void parseForExpression() {
    final PsiBuilder.Marker forExpression = myBuilder.mark();
    advanceAndSkipNewLines();
    checkMatches(TheRTokenTypes.LPAR, "( expected");
    parseExpressionStatement(true);
    skipNewLines();
    if (checkMatches(TheRTokenTypes.IN_KEYWORD, "'in' expected")) {
      parseExpressionStatement(true);
    }
    skipNewLines();
    checkMatches(TheRTokenTypes.RPAR, ") expected");
    parseExpressionStatement(false);
    checkSemicolon();
    forExpression.done(TheRElementTypes.FOR_STATEMENT);
  }

  private void parseRepeatExpression() {
    final PsiBuilder.Marker repeatExpression = myBuilder.mark();
    advanceAndSkipNewLines();
    parseExpressionStatement(false);
    checkSemicolon();
    repeatExpression.done(TheRElementTypes.REPEAT_STATEMENT);
  }

  private void parseBreakExpression() {
    final PsiBuilder.Marker breakExpression = myBuilder.mark();
    myBuilder.advanceLexer();
    parseNextBreakInnerExpression();
    checkSemicolon();
    breakExpression.done(TheRElementTypes.BREAK_STATEMENT);
  }

  private void parseNextExpression() {
    final PsiBuilder.Marker statement = myBuilder.mark();
    myBuilder.advanceLexer();
    parseNextBreakInnerExpression();
    skipNewLines();
    statement.done(TheRElementTypes.NEXT_STATEMENT);
  }

  private void parseNextBreakInnerExpression() {
    if (myBuilder.getTokenType() == TheRTokenTypes.LPAR) {
      advanceAndSkipNewLines();
      if (atToken(TheRTokenTypes.RPAR)) {
        myBuilder.advanceLexer();
        return;
      }
      parseExpressionStatement(false);
      skipNewLines();
      checkMatches(TheRTokenTypes.RPAR, ") expected");
    }
  }

  public void parseBlockExpression() {
    final PsiBuilder.Marker blockExpression = myBuilder.mark();
    advanceAndSkipNewLines();
    while (myBuilder.getTokenType() != TheRTokenTypes.RBRACE) {
      if (myBuilder.eof()) {
        myBuilder.error("missing }");
        blockExpression.done(TheRElementTypes.BLOCK);
        return;
      }
      parseExpressionStatement(false);
    }

    myBuilder.advanceLexer();
    checkSemicolon();
    blockExpression.done(TheRElementTypes.BLOCK);
  }

  public void parseHelpExpression() {
    final PsiBuilder.Marker mark = myBuilder.mark();
    advanceAndSkipNewLines();
    if (myBuilder.getTokenType() == TheRTokenTypes.HELP) {
      advanceAndSkipNewLines();
    }
    if (TheRTokenTypes.KEYWORDS.contains(myBuilder.getTokenType())) {
      myBuilder.advanceLexer();
      mark.done(TheRElementTypes.HELP_EXPRESSION);
    }
    else if (parseFormulaeExpression(false)) {    // should we parse expression statement instead?
      mark.done(TheRElementTypes.HELP_EXPRESSION);
    }
    else {
      myBuilder.error(EXPRESSION_EXPECTED);
      mark.drop();
    }
  }

  protected void parseAssignmentExpression(boolean skipLineBreak) {
    final PsiBuilder.Marker assignmentExpression = myBuilder.mark();
    final boolean successfull = parseFormulaeExpression(skipLineBreak);
    if (successfull) {
      if (TheRTokenTypes.ASSIGNMENTS.contains(myBuilder.getTokenType())) {
        advanceAndSkipNewLines();
        parseExpressionStatement(skipLineBreak);
        checkSemicolon();
        assignmentExpression.done(TheRElementTypes.ASSIGNMENT_STATEMENT);
      }
      else {
        checkSemicolon();
        assignmentExpression.drop();
      }
      return;
    }
    else
      assignmentExpression.drop();

    myBuilder.advanceLexer();
    myBuilder.error(EXPRESSION_EXPECTED);
  }

  public boolean parseFormulaeExpression(boolean skipLineBreak) {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseOrExpression(skipLineBreak)) {
      expr.drop();
      return false;
    }
    skipNewLines(skipLineBreak);
    while (TheRTokenTypes.TILDE == myBuilder.getTokenType()) {
      advanceAndSkipNewLines();
      if (!parseOrExpression(skipLineBreak)) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  public boolean parseOrExpression(boolean skipLineBreak) {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseANDExpression(skipLineBreak)) {
      expr.drop();
      return false;
    }
    skipNewLines(skipLineBreak);
    while (TheRTokenTypes.OR_OPERATIONS.contains(myBuilder.getTokenType())) {
      advanceAndSkipNewLines();
      if (!parseANDExpression(skipLineBreak)) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  private boolean parseANDExpression(boolean skipLineBreak) {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseNOTExpression(skipLineBreak)) {
      expr.drop();
      return false;
    }
    skipNewLines(skipLineBreak);
    while (TheRTokenTypes.AND_OPERATIONS.contains(myBuilder.getTokenType())) {
      advanceAndSkipNewLines();
      if (!parseNOTExpression(skipLineBreak)) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  private boolean parseNOTExpression(boolean skipLineBreak) {
    if (myBuilder.getTokenType() == TheRTokenTypes.NOT) {
      final PsiBuilder.Marker expr = myBuilder.mark();
      myBuilder.advanceLexer();
      skipNewLines(skipLineBreak);
      if (!parseNOTExpression(skipLineBreak)) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.PREFIX_EXPRESSION);
      return true;
    }
    else {
      return parseComparisonExpression(skipLineBreak);
    }
  }

  private boolean parseComparisonExpression(boolean skipLineBreak) {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseAdditiveExpression(skipLineBreak)) {
      myBuilder.error(EXPRESSION_EXPECTED);
      expr.drop();
      return false;
    }
    skipNewLines(skipLineBreak);
    while (TheRTokenTypes.COMPARISON_OPERATIONS.contains(myBuilder.getTokenType())) {
      advanceAndSkipNewLines();
      if (!parseAdditiveExpression(skipLineBreak)) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  private boolean parseAdditiveExpression(boolean skipLineBreak) {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseMultiplicativeExpression(skipLineBreak)) {
      expr.drop();
      return false;
    }
    skipNewLines(skipLineBreak);
    while (TheRTokenTypes.ADDITIVE_OPERATIONS.contains(myBuilder.getTokenType())) {
      advanceAndSkipNewLines();
      if (!parseMultiplicativeExpression(skipLineBreak)) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  private boolean parseMultiplicativeExpression(boolean skipLineBreak) {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseUserDefinedExpression(skipLineBreak)) {
      expr.drop();
      return false;
    }
    skipNewLines(skipLineBreak);
    while (TheRTokenTypes.MULTIPLICATIVE_OPERATIONS.contains(myBuilder.getTokenType())) {
      advanceAndSkipNewLines();
      if (!parseUserDefinedExpression(skipLineBreak)) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }


  private boolean parseUserDefinedExpression(boolean skipLineBreak) {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseSliceExpression(skipLineBreak)) {
      expr.drop();
      return false;
    }
    skipNewLines(skipLineBreak);
    while (TheRTokenTypes.INFIX_OP == myBuilder.getTokenType() || TheRTokenTypes.MATCHING == myBuilder.getTokenType()) {
      advanceAndSkipNewLines();
      if (!parseSliceExpression(skipLineBreak)) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  protected boolean parseSliceExpression(boolean skipLineBreak) {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseUnaryExpression(skipLineBreak)) {
      expr.drop();
      return false;
    }
    skipNewLines(skipLineBreak);
    while (TheRTokenTypes.COLON == myBuilder.getTokenType()) {
      advanceAndSkipNewLines();
      if (!parseUnaryExpression(skipLineBreak)) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.SLICE_EXPRESSION);
      expr = expr.precede();
    }

    expr.drop();
    return true;
  }

  protected boolean parseUnaryExpression(boolean skipLineBreak) {
    final IElementType tokenType = myBuilder.getTokenType();
    if (TheRTokenTypes.UNARY_OPERATIONS.contains(tokenType)) {
      final PsiBuilder.Marker expr = myBuilder.mark();
      myBuilder.advanceLexer();
      skipNewLines(skipLineBreak);
      if (!parseUnaryExpression(skipLineBreak)) {
        myBuilder.error(EXPRESSION_EXPECTED);
      }
      expr.done(TheRElementTypes.PREFIX_EXPRESSION);
      return true;
    }
    else {
      return parsePowerExpression(skipLineBreak);
    }
  }

  private boolean parsePowerExpression(boolean skipLineBreak) {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseMemberExpression()) {
      expr.drop();
      return false;
    }
    skipNewLines(skipLineBreak);
    if (TheRTokenTypes.POWER_OPERATIONS.contains(myBuilder.getTokenType())) {
      myBuilder.advanceLexer();
      if (!parseUnaryExpression(skipLineBreak)) {
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
    if (TheRTokenTypes.STATEMENT_START_TOKENS.contains(myBuilder.getTokenType())) {
      parseExpressionStatement(false);
      expr.drop();
      return true;
    }
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
      else if (tokenType == TheRTokenTypes.AT) {
        myBuilder.advanceLexer();
        if (!parseStringOrIdentifier()) {
          myBuilder.error("Expected string or identifier");
        }
        expr.done(TheRElementTypes.REFERENCE_EXPRESSION);
        expr = expr.precede();
      }
      else if (tokenType == TheRTokenTypes.LPAR) {
        skipNewLines();
        parseArgumentList();
        expr.done(TheRElementTypes.CALL_EXPRESSION);
        expr = expr.precede();
      }
      else if (TheRTokenTypes.NAMESPACE_ACCESS.contains(tokenType)) {
        myBuilder.advanceLexer();
        parseFormulaeExpression(false);
        expr.done(TheRElementTypes.REFERENCE_EXPRESSION);
        expr = expr.precede();
      }
      else if (TheRTokenTypes.OPEN_BRACKETS.contains(tokenType)) {
        advanceAndSkipNewLines();
        if (myBuilder.getTokenType() == TheRTokenTypes.COMMA) {
          PsiBuilder.Marker marker = myBuilder.mark();
          marker.done(TheRElementTypes.EMPTY_EXPRESSION);
          advanceAndSkipNewLines();
        }
        final IElementType CLOSE_BRACKET = TheRTokenTypes.BRACKER_PAIRS.get(tokenType);
        while (myBuilder.getTokenType() != CLOSE_BRACKET && !myBuilder.eof()) {
          if (myBuilder.getTokenType() == TheRTokenTypes.COMMA) {
            PsiBuilder.Marker marker = myBuilder.mark();
            marker.done(TheRElementTypes.EMPTY_EXPRESSION);
            advanceAndSkipNewLines();
            continue;
          }
          parseExpressionStatement(true);
          if (myBuilder.getTokenType() == TheRTokenTypes.COMMA) {
            advanceAndSkipNewLines();
          }
        }

        checkMatches(CLOSE_BRACKET, "Closing ] or ]] expected");
        expr.done(TheRElementTypes.SUBSCRIPTION_EXPRESSION);
        expr = expr.precede();
      }
      //else if (myBuilder.getTokenType() == TheRTokenTypes.IF_KEYWORD) {
      //  parseIfExpression();
      //  expr.done(TheRElementTypes.IF_STATEMENT);
      //  expr = expr.precede();
      //}
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
    else if (myBuilder.getTokenType() == TheRTokenTypes.TRIPLE_DOTS) {
      myBuilder.advanceLexer();
      return true;
    }
    return false;
  }

  public void parseArgumentList() {
    LOG.assertTrue(myBuilder.getTokenType() == TheRTokenTypes.LPAR);
    final PsiBuilder.Marker arglist = myBuilder.mark();
    myBuilder.advanceLexer();
    int argNumber = 0;
    skipNewLines();
    while (myBuilder.getTokenType() != TheRTokenTypes.RPAR) {
      argNumber++;
      if (argNumber > 1) {
        if (matchToken(TheRTokenTypes.COMMA)) {
          skipNewLines();
          if (atToken(TheRTokenTypes.RPAR)) {
            break;
          }
        }
        else {
          myBuilder.error("',' or ')' expected");
          break;
        }
      }
      if (atToken(TheRTokenTypes.COMMA)) {
        final PsiBuilder.Marker mark = myBuilder.mark();
        mark.done(TheRElementTypes.EMPTY_EXPRESSION);
        skipNewLines();
        continue;
      }
      if (myBuilder.getTokenType() == TheRTokenTypes.IDENTIFIER || myBuilder.getTokenType() == TheRTokenTypes.STRING_LITERAL ||
        myBuilder.getTokenType() == TheRTokenTypes.TICK || TheRTokenTypes.SPECIAL_CONSTANTS.contains(myBuilder.getTokenType())) {
        final PsiBuilder.Marker keywordArgMarker = myBuilder.mark();
        parseFormulaeExpression(false);
        skipNewLines();
        if (TheRTokenTypes.ASSIGNMENTS.contains(myBuilder.getTokenType())) {
          advanceAndSkipNewLines();
          if (TheRTokenTypes.STATEMENT_START_TOKENS.contains(myBuilder.getTokenType())) {
            parseExpressionStatement(false);
            keywordArgMarker.done(TheRElementTypes.KEYWORD_ARGUMENT_EXPRESSION);
            skipNewLines();
            continue;
          }
          final PsiBuilder.Marker keywordValue = myBuilder.mark();
          if (!parseFormulaeExpression(false)) {
            myBuilder.error(EXPRESSION_EXPECTED);
            keywordValue.rollbackTo();
          }
          else {
            keywordValue.drop();
          }
          keywordArgMarker.done(TheRElementTypes.KEYWORD_ARGUMENT_EXPRESSION);
          skipNewLines();
          continue;
        }
        keywordArgMarker.rollbackTo();
      }
      if (myBuilder.getTokenType() == TheRTokenTypes.LBRACE) {
        parseBlockExpression();
        skipNewLines();
        continue;
      }
      if (myBuilder.getTokenType() == TheRTokenTypes.TRIPLE_DOTS) {
        myBuilder.advanceLexer();
        skipNewLines();
        continue;
      }
      if (TheRTokenTypes.STATEMENT_START_TOKENS.contains(myBuilder.getTokenType())) {
        parseExpressionStatement(false);
        skipNewLines();
        continue;
      }
      if (!parseFormulaeExpression(true)) {
        myBuilder.error(EXPRESSION_EXPECTED);
        break;
      }
      skipNewLines();
    }
    skipNewLines();
    checkMatches(TheRTokenTypes.RPAR, "')' expected");
    arglist.done(TheRElementTypes.ARGUMENT_LIST);
  }

  public void parseReprExpression() {
    LOG.assertTrue(myBuilder.getTokenType() == TheRTokenTypes.TICK);
    final PsiBuilder.Marker expr = myBuilder.mark();
    myBuilder.advanceLexer();
    while (!atToken(TheRTokenTypes.TICK) && !myBuilder.eof()) {
      myBuilder.advanceLexer();
    }
    checkMatches(TheRTokenTypes.TICK, "'`' (backtick) expected");
    expr.done(TheRElementTypes.REPR_EXPRESSION);
  }

  private void parseParenthesizedExpression() {
    final PsiBuilder.Marker expr = myBuilder.mark();
    myBuilder.advanceLexer();
    parseExpressionStatement(true);
    checkMatches(TheRTokenTypes.RPAR, ") expected");
    expr.done(TheRElementTypes.PARENTHESIZED_EXPRESSION);
  }


  public boolean parsePrimaryExpression() {
    final IElementType firstToken = myBuilder.getTokenType();
    if (firstToken == TheRTokenTypes.NUMERIC_LITERAL) {
      buildTokenElement(TheRElementTypes.INTEGER_LITERAL_EXPRESSION, myBuilder);
      return true;
    }
    else if (firstToken == TheRTokenTypes.INTEGER_LITERAL) {
      buildTokenElement(TheRElementTypes.INTEGER_LITERAL_EXPRESSION, myBuilder);
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
    else if (firstToken == TheRTokenTypes.DOT) {
      buildTokenElement(TheRElementTypes.REFERENCE_EXPRESSION, myBuilder);
      return true;
    }
    else if (TheRTokenTypes.NA_KEYWORDS.contains(firstToken)) {
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
    else if (firstToken == TheRTokenTypes.LBRACE) {
      parseBlockExpression();
      return true;
    }
    else {
      myBuilder.advanceLexer();
    }
    return false;
  }

}