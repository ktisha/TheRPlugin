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
    parseFunctionBody();
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
    while (myBuilder.getTokenType() != TheRTokenTypes.RPAR) {
      if (first) {
        first = false;
      }
      else {
        if (myBuilder.getTokenType() == TheRTokenTypes.COMMA) {
          myBuilder.advanceLexer();
        }
        else {
          myBuilder.error(", or ) expected");
          break;
        }
      }

      final PsiBuilder.Marker parameter = myBuilder.mark();
      if (myBuilder.getTokenType() == TheRTokenTypes.IDENTIFIER) {
        myBuilder.advanceLexer();
        parameter.done(TheRElementTypes.PARAMETER);
      }
      else {
        myBuilder.error("parameter name expected");
        parameter.rollbackTo();
      }
    }

    if (myBuilder.getTokenType() == TheRTokenTypes.RPAR) {
      myBuilder.advanceLexer();
    }

    parameterList.done(TheRElementTypes.PARAMETER_LIST);
  }

  private void parseFunctionBody() {
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
      parseFunctionDeclaration();
    }
    else {
      getStatementParser().parseStatement();
    }
  }
}
