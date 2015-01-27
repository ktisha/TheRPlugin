package com.jetbrains.ther.parsing;

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
import com.jetbrains.ther.psi.TheRElementType;
import com.jetbrains.ther.psi.TheRFileElementType;
import com.jetbrains.ther.psi.TheRFileImpl;
import org.jetbrains.annotations.NotNull;

public class TheRParserDefinition implements ParserDefinition {
  private final TokenSet myWhitespaceTokens;
  private final TokenSet myCommentTokens;
  private final TokenSet myStringLiteralTokens;

  public static IFileElementType FILE = new TheRFileElementType();
  public static IElementType END_OF_LINE_COMMENT = new TheRElementType("END_OF_LINE_COMMENT");
  public static IElementType BAD_CHARACTER = new TheRElementType("BAD_CHARACTER");
  public static IElementType SPACE = new TheRElementType("SPACE");
  public static IElementType TAB = new TheRElementType("TAB");
  public static IElementType FORMFEED = new TheRElementType("FORMFEED");


  public TheRParserDefinition() {
    myWhitespaceTokens = TokenSet.create(SPACE, TAB, FORMFEED);
    myCommentTokens = TokenSet.create(END_OF_LINE_COMMENT);
    myStringLiteralTokens = TokenSet.create(TheRElementTypes.THE_R_STRING);
  }

  @Override
  @NotNull
  public Lexer createLexer(Project project) {
    return new TheRLexer();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
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
    return TheRElementTypes.Factory.createElement(node);
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
