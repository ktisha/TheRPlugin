package com.jetbrains.ther;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.ther.lexer.TheRLexer;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.parsing.TheRParserDefinition;
import com.jetbrains.ther.psi.api.TheRReferenceExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRFindUsagesProvider implements FindUsagesProvider {
  @Nullable
  @Override
  public WordsScanner getWordsScanner() {
    return new DefaultWordsScanner(new TheRLexer(), TokenSet.create(TheRElementTypes.THE_R_IDENTIFIER),
                                   TokenSet.create(TheRParserDefinition.END_OF_LINE_COMMENT),
                                   TokenSet.create(TheRElementTypes.THE_R_STRING_LITERAL_EXPRESSION));
  }

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    return psiElement instanceof PsiNamedElement || psiElement instanceof TheRReferenceExpression;
  }

  @Nullable
  @Override
  public String getHelpId(@NotNull PsiElement psiElement) {
    return null;
  }

  @NotNull
  @Override
  public String getType(@NotNull PsiElement element) {
    return "TheRElement";
  }

  @NotNull
  @Override
  public String getDescriptiveName(@NotNull PsiElement element) {
    return "THeRElement";
  }

  @NotNull
  @Override
  public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
    return element.getText();
  }
}
