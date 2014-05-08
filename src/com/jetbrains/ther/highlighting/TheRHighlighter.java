package com.jetbrains.ther.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import com.jetbrains.ther.lexer.TheRLexer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TheRHighlighter extends SyntaxHighlighterBase {
  private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<IElementType, TextAttributesKey>();

  static {
    fillMap(ATTRIBUTES, TheRTokenTypes.RESERVED_WORDS, TheRSyntaxHighlighterColors.KEYWORD);

    fillMap(ATTRIBUTES, TheRTokenTypes.OPERATORS, TheRSyntaxHighlighterColors.OPERATION_SIGN);

    ATTRIBUTES.put(TheRTokenTypes.STRING_LITERAL, TheRSyntaxHighlighterColors.STRING);
    ATTRIBUTES.put(TheRTokenTypes.NUMERIC_LITERAL, TheRSyntaxHighlighterColors.NUMBER);
    ATTRIBUTES.put(TheRTokenTypes.COMPLEX_LITERAL, TheRSyntaxHighlighterColors.NUMBER);
    ATTRIBUTES.put(TheRTokenTypes.INTEGER_LITERAL, TheRSyntaxHighlighterColors.NUMBER);


    ATTRIBUTES.put(TheRTokenTypes.LPAR, TheRSyntaxHighlighterColors.PARENTHS);
    ATTRIBUTES.put(TheRTokenTypes.RPAR, TheRSyntaxHighlighterColors.PARENTHS);

    ATTRIBUTES.put(TheRTokenTypes.LBRACE, TheRSyntaxHighlighterColors.BRACES);
    ATTRIBUTES.put(TheRTokenTypes.RBRACE, TheRSyntaxHighlighterColors.BRACES);

    ATTRIBUTES.put(TheRTokenTypes.LBRACKET, TheRSyntaxHighlighterColors.BRACKETS);
    ATTRIBUTES.put(TheRTokenTypes.LDBRACKET, TheRSyntaxHighlighterColors.BRACKETS);
    ATTRIBUTES.put(TheRTokenTypes.RBRACKET, TheRSyntaxHighlighterColors.BRACKETS);
    ATTRIBUTES.put(TheRTokenTypes.RDBRACKET, TheRSyntaxHighlighterColors.BRACKETS);

    ATTRIBUTES.put(TheRTokenTypes.COMMA, TheRSyntaxHighlighterColors.COMMA);
    ATTRIBUTES.put(TheRTokenTypes.DOT, TheRSyntaxHighlighterColors.DOT);
    ATTRIBUTES.put(TheRTokenTypes.SEMICOLON, TheRSyntaxHighlighterColors.SEMICOLON);

    ATTRIBUTES.put(TheRTokenTypes.END_OF_LINE_COMMENT, TheRSyntaxHighlighterColors.LINE_COMMENT);

    ATTRIBUTES.put(TheRTokenTypes.BAD_CHARACTER, TheRSyntaxHighlighterColors.BAD_CHARACTER);
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
