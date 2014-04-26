package com.jetbrains.ther.parsing;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.ther.lexer.TheRLexer;
import com.jetbrains.ther.lexer.TheRTokenTypes;
import com.jetbrains.ther.psi.TheRElementType;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.psi.TheRFileImpl;
import org.jetbrains.annotations.NotNull;

public class TheRParserDefinition implements ParserDefinition {
  private final TokenSet myWhitespaceTokens;
  private final TokenSet myCommentTokens;
  private final TokenSet myStringLiteralTokens;

  public TheRParserDefinition() {
    myWhitespaceTokens = TokenSet.create(TheRTokenTypes.LINE_BREAK, TheRTokenTypes.SPACE, TheRTokenTypes.TAB);
    myCommentTokens = TokenSet.create(TheRTokenTypes.END_OF_LINE_COMMENT);
    myStringLiteralTokens = TokenSet.create(TheRTokenTypes.STRING_LITERAL);
  }

  @Override
  @NotNull
  public Lexer createLexer(Project project) {
    return new TheRLexer();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return TheRElementTypes.FILE;
  }

  @Override
  @NotNull
  public TokenSet getWhitespaceTokens() {
    return myWhitespaceTokens;
  }

  @Override
  @NotNull
  public TokenSet getCommentTokens() {
    return myCommentTokens;
  }

  @Override
  @NotNull
  public TokenSet getStringLiteralElements() {
    return myStringLiteralTokens;
  }

  @Override
  @NotNull
  public PsiParser createParser(Project project) {
    return new TheRParser();
  }

  @Override
  @NotNull
  public PsiElement createElement(@NotNull ASTNode node) {
    final IElementType type = node.getElementType();
    if (type instanceof TheRElementType) {
      TheRElementType elementType = (TheRElementType)type;
      return elementType.createElement(node);
    }
    return new ASTWrapperPsiElement(node);
  }

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new TheRFileImpl(viewProvider);
  }

  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }

}
