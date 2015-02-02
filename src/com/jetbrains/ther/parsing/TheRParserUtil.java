package com.jetbrains.ther.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;

/**
 * @author Alefas
 * @since 23/12/14.
 */
public class TheRParserUtil extends GeneratedParserUtilBase {
  public static boolean parseEmptyExpression(PsiBuilder builder, int level) {
    PsiBuilder.Marker emptyMarker = builder.mark();
    emptyMarker.done(TheRElementTypes.THE_R_EMPTY_EXPRESSION);
    return true;
  }
}
