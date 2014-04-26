package com.jetbrains.ther.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.ther.TheRFileType;
import com.jetbrains.ther.psi.api.TheRElement;
import org.jetbrains.annotations.NotNull;

public class TheRBaseElementImpl extends ASTWrapperPsiElement implements TheRElement {
  public TheRBaseElementImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public Language getLanguage() {
    return TheRFileType.INSTANCE.getLanguage();
  }

  @Override
  public String toString() {
    return getNode().getElementType().toString();
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
  }
}
