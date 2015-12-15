package com.jetbrains.ther.psi;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.jetbrains.ther.TheRFileType;
import com.jetbrains.ther.psi.api.TheRPsiElement;
import org.jetbrains.annotations.NotNull;

public class TheRBaseElementImpl<T extends StubElement> extends StubBasedPsiElementBase<T> implements TheRPsiElement {
  public TheRBaseElementImpl(@NotNull final ASTNode node) {
    super(node);
  }

  public TheRBaseElementImpl(@NotNull final T stub, @NotNull final IStubElementType nodeType) {
    super(stub, nodeType);
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
    visitor.visitElement(this);
  }
}
