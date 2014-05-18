package com.jetbrains.ther.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import org.jetbrains.annotations.NotNull;

public class TheRFunctionParsing extends Parsing {
  private static final Logger LOG = Logger.getInstance(TheRFunctionParsing.class.getName());
  public TheRFunctionParsing(@NotNull final TheRParsingContext context) {
    super(context);
  }

  public void parseFunctionDeclaration() {
    LOG.assertTrue(myBuilder.getTokenType() == TheRTokenTypes.FUNCTION_KEYWORD);

    final PsiBuilder.Marker functionMarker = myBuilder.mark();
    myBuilder.advanceLexer();

    parseParameterList();
    getExpressionParser().parseExpressionStatement(false);
    functionMarker.done(TheRElementTypes.FUNCTION_DECLARATION);
  }

  private void parseParameterList() {
    final PsiBuilder.Marker parameterList = myBuilder.mark();
    if (myBuilder.getTokenType() != TheRTokenTypes.LPAR) {
      parameterList.rollbackTo();
      myBuilder.error("( expected");
      return;
    }
    else {
      myBuilder.advanceLexer();
    }

    boolean first = true;
    skipNewLines();
    while (myBuilder.getTokenType() != TheRTokenTypes.RPAR) {
      if (first) {
        first = false;
      }
      else {
        if (myBuilder.getTokenType() == TheRTokenTypes.COMMA) {
          myBuilder.advanceLexer();
          skipNewLines();
        }
        else {
          myBuilder.error(", or ) expected");
          break;
        }
      }

      final PsiBuilder.Marker parameter = myBuilder.mark();
      if (myBuilder.getTokenType() == TheRTokenTypes.IDENTIFIER) {
        advanceAndSkipNewLines();
        if (matchToken(TheRTokenTypes.EQ)) {
          skipNewLines();
          if (myBuilder.getTokenType() == TheRTokenTypes.RPAR) {
            PsiBuilder.Marker invalidElements = myBuilder.mark();
            invalidElements.error(EXPRESSION_EXPECTED);
            parameter.done(TheRElementTypes.PARAMETER);
            break;
          }
          if (!getExpressionParser().parseFormulaeExpression(false)) {
            PsiBuilder.Marker invalidElements = myBuilder.mark();
            while(!atToken(TheRTokenTypes.COMMA)) {
              myBuilder.advanceLexer();
            }
            invalidElements.error(EXPRESSION_EXPECTED);
          }
        }
        parameter.done(TheRElementTypes.PARAMETER);
      }
      else if (myBuilder.getTokenType() == TheRTokenTypes.TICK) {
        getExpressionParser().parseReprExpression();
        parameter.done(TheRElementTypes.PARAMETER);
      }
      else if (myBuilder.getTokenType() == TheRTokenTypes.TRIPLE_DOTS) {
        myBuilder.advanceLexer();
        parameter.done(TheRElementTypes.PARAMETER);
      }
      else {
        myBuilder.error("parameter name expected");
        parameter.rollbackTo();
      }
      skipNewLines();
    }

    if (myBuilder.getTokenType() == TheRTokenTypes.RPAR) {
      myBuilder.advanceLexer();
    }

    parameterList.done(TheRElementTypes.PARAMETER_LIST);
  }

}
