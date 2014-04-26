package com.jetbrains.ther.parsing;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class TheRParser implements PsiParser {
  private static final Logger LOGGER = Logger.getInstance(TheRParser.class.getName());

  @Override
  @NotNull
  public ASTNode parse(@NotNull final IElementType root, @NotNull final PsiBuilder builder) {
    long start = System.currentTimeMillis();
    final PsiBuilder.Marker rootMarker = builder.mark();

    final TheRParsingContext context = createParsingContext(builder);
    final TheRStatementParsing statementParser = context.getStatementParser();
    while (!builder.eof()) {
      statementParser.parseStatement();
    }
    rootMarker.done(root);
    final ASTNode ast = builder.getTreeBuilt();
    long diff = System.currentTimeMillis() - start;
    double kb = builder.getCurrentOffset() / 1000.0;
    LOGGER.debug("Parsed " + String.format("%.1f", kb) + "K file in " + diff + "ms");
    return ast;
  }

  @NotNull
  protected TheRParsingContext createParsingContext(@NotNull final PsiBuilder builder) {
    return new TheRParsingContext(builder);
  }
}
