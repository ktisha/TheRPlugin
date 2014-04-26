package com.jetbrains.ther.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import org.jetbrains.annotations.NotNull;


public class TheRStatementParsing extends Parsing {
  private static final Logger LOG = Logger.getInstance(TheRStatementParsing.class.getName());


  public TheRStatementParsing(@NotNull final TheRParsingContext context) {
    super(context);
  }

  public void parseStatement() {
    final IElementType firstToken = myBuilder.getTokenType();
    if (firstToken == null) return;

    if (firstToken == TheRTokenTypes.IF_KEYWORD) {
      parseIfStatement();
      return;
    }
    if (firstToken == TheRTokenTypes.WHILE_KEYWORD) {
      parseWhileStatement();
      return;
    }
    if (firstToken == TheRTokenTypes.LBRACE) {
      parseBlock();
      return;
    }
    parseSimpleStatement();
  }

  protected void parseSimpleStatement() {
    final IElementType tokenType = myBuilder.getTokenType();
    if (tokenType == null) return;

    final PsiBuilder.Marker exprStatement = myBuilder.mark();
    final TheRExpressionParsing expressionParser = getExpressionParser();
    final boolean successfull = expressionParser.parseExpression();
    if (successfull) {
      if (TheRTokenTypes.ASSIGNMENTS.contains(myBuilder.getTokenType())) {
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() == TheRTokenTypes.FUNCTION_KEYWORD) {
          getFunctionParser().parseFunctionDeclaration();
        }
        else if (!expressionParser.parseExpression()) {
          myBuilder.error(EXPRESSION_EXPECTED);
        }
        exprStatement.done(TheRElementTypes.ASSIGNMENT_STATEMENT);
      }
      else {
        exprStatement.done(TheRElementTypes.EXPRESSION_STATEMENT);
      }
      return;
    }
    else
      exprStatement.drop();

    myBuilder.advanceLexer();
    myBuilder.error("Statement expected, found " + tokenType.toString());
  }

  private void parseIfStatement() {
    LOG.assertTrue(myBuilder.getTokenType() == TheRTokenTypes.IF_KEYWORD);
    final PsiBuilder.Marker ifStatement = myBuilder.mark();
    myBuilder.advanceLexer();

    checkMatches(TheRTokenTypes.LPAR, "( expected");
    getExpressionParser().parseExpression();
    checkMatches(TheRTokenTypes.RPAR, ") expected");

    parseStatement();

    if (myBuilder.getTokenType() == TheRTokenTypes.ELSE_KEYWORD) {
      myBuilder.advanceLexer();
      parseStatement();
    }

    ifStatement.done(TheRElementTypes.IF_STATEMENT);
  }
  
  private void parseWhileStatement() {
    LOG.assertTrue(myBuilder.getTokenType() == TheRTokenTypes.WHILE_KEYWORD);
    final PsiBuilder.Marker statement = myBuilder.mark();
    myBuilder.advanceLexer();

    checkMatches(TheRTokenTypes.LPAR, "( expected");
    getExpressionParser().parseExpression();
    checkMatches(TheRTokenTypes.RPAR, ") expected");

    parseStatement();
    statement.done(TheRElementTypes.WHILE_STATEMENT);
  }


  public void parseBlock() {
    if (myBuilder.getTokenType() != TheRTokenTypes.LBRACE) {
      myBuilder.error("statements block expected");
      return;
    }

    final PsiBuilder.Marker block = myBuilder.mark();
    myBuilder.advanceLexer();
    while (myBuilder.getTokenType() != TheRTokenTypes.RBRACE) {
      if (myBuilder.eof()) {
        myBuilder.error("missing }");
        block.done(TheRElementTypes.BLOCK);
        return;
      }

      parseSourceElement();
    }

    myBuilder.advanceLexer();
    block.done(TheRElementTypes.BLOCK);
  }

  public void parseSourceElement() {
    if (myBuilder.getTokenType() == TheRTokenTypes.FUNCTION_KEYWORD) {
      getFunctionParser().parseFunctionDeclaration();
    }
    else {
      getStatementParser().parseStatement();
    }
  }

}
