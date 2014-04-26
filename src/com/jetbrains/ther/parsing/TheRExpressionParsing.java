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
      final PsiBuilder.Marker slice = myBuilder.mark();
      buildTokenElement(TheRElementTypes.INTEGER_LITERAL_EXPRESSION, myBuilder);
      if (matchToken(TheRTokenTypes.COLON)) {
        if (myBuilder.getTokenType() == TheRTokenTypes.NUMERIC_LITERAL) {
          buildTokenElement(TheRElementTypes.INTEGER_LITERAL_EXPRESSION, myBuilder);
          slice.done(TheRElementTypes.SLICE_EXPRESSION);
        }
      }
      else {
        slice.drop();
      }
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


}
