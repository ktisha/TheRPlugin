package com.jetbrains.ther.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.ther.lexer.TheRLexer;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.parsing.TheRParserDefinition;
import com.jetbrains.ther.psi.TheRPsiImplUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TheRHighlighter extends SyntaxHighlighterBase {
  private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<IElementType, TextAttributesKey>();

  static {
    fillMap(ATTRIBUTES, TheRPsiImplUtil.RESERVED_WORDS, TheRSyntaxHighlighterColors.KEYWORD);

    fillMap(ATTRIBUTES, TheRPsiImplUtil.OPERATORS, TheRSyntaxHighlighterColors.OPERATION_SIGN);

    ATTRIBUTES.put(TheRElementTypes.THE_R_STRING, TheRSyntaxHighlighterColors.STRING);
    ATTRIBUTES.put(TheRElementTypes.THE_R_NUMERIC, TheRSyntaxHighlighterColors.NUMBER);
    ATTRIBUTES.put(TheRElementTypes.THE_R_COMPLEX, TheRSyntaxHighlighterColors.NUMBER);
    ATTRIBUTES.put(TheRElementTypes.THE_R_INTEGER, TheRSyntaxHighlighterColors.NUMBER);


    ATTRIBUTES.put(TheRElementTypes.THE_R_LPAR, TheRSyntaxHighlighterColors.PARENTHS);
    ATTRIBUTES.put(TheRElementTypes.THE_R_RPAR, TheRSyntaxHighlighterColors.PARENTHS);

    ATTRIBUTES.put(TheRElementTypes.THE_R_LBRACE, TheRSyntaxHighlighterColors.BRACES);
    ATTRIBUTES.put(TheRElementTypes.THE_R_RBRACE, TheRSyntaxHighlighterColors.BRACES);

    ATTRIBUTES.put(TheRElementTypes.THE_R_LBRACKET, TheRSyntaxHighlighterColors.BRACKETS);
    ATTRIBUTES.put(TheRElementTypes.THE_R_LDBRACKET, TheRSyntaxHighlighterColors.BRACKETS);
    ATTRIBUTES.put(TheRElementTypes.THE_R_RBRACKET, TheRSyntaxHighlighterColors.BRACKETS);
    ATTRIBUTES.put(TheRElementTypes.THE_R_RDBRACKET, TheRSyntaxHighlighterColors.BRACKETS);

    ATTRIBUTES.put(TheRElementTypes.THE_R_COMMA, TheRSyntaxHighlighterColors.COMMA);
    ATTRIBUTES.put(TheRElementTypes.THE_R_SEMI, TheRSyntaxHighlighterColors.SEMICOLON);

    ATTRIBUTES.put(TheRParserDefinition.END_OF_LINE_COMMENT, TheRSyntaxHighlighterColors.LINE_COMMENT);

    ATTRIBUTES.put(TheRParserDefinition.BAD_CHARACTER, TheRSyntaxHighlighterColors.BAD_CHARACTER);
  }

  @Override
  @NotNull
  public Lexer getHighlightingLexer() {
    return new TheRLexer();
  }

  @Override
  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return pack(ATTRIBUTES.get(tokenType));
  }
}
