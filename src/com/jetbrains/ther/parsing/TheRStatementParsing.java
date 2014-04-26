package com.jetbrains.ther.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import com.jetbrains.ther.parsing.TheRElementTypes;
import org.jetbrains.annotations.NotNull;


public class TheRStatementParsing extends Parsing {
  private static final Logger LOG = Logger.getInstance(TheRStatementParsing.class.getName());

  private static final String EXPRESSION_EXPECTED = "Expression expected";
  public static final String IDENTIFIER_EXPECTED = "Identifier expected";

  public TheRStatementParsing(@NotNull final TheRParsingContext context) {
    super(context);
  }

  public void parseStatement() {
    final IElementType firstToken = myBuilder.getTokenType();
    if (firstToken == null) return;

    //if (firstToken == TheRTokenTypes.IF_KEYWORD) {
    //  //parseIfStatement();
    //  return;
    //}
    parseSimpleStatement();
  }

  protected void parseSimpleStatement() {
    final IElementType tokenType = myBuilder.getTokenType();
    if (tokenType == null) return;

    final PsiBuilder.Marker exprStatement = myBuilder.mark();
    final TheRExpressionParsing expressionParser = getExpressionParser();
    final boolean successfull = expressionParser.parsePrimaryExpression();
    if (successfull) {
      if (TheRTokenTypes.ASSIGNMENTS.contains(myBuilder.getTokenType())) {
        myBuilder.advanceLexer();
        if (!expressionParser.parsePrimaryExpression()) {
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

}
