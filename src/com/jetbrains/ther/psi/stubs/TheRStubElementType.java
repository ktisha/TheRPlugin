package com.jetbrains.ther.psi.stubs;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.jetbrains.ther.TheRFileType;
import com.jetbrains.ther.psi.api.TheRPsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class TheRStubElementType<StubT extends StubElement, PsiT extends TheRPsiElement> extends IStubElementType<StubT, PsiT> {
  public TheRStubElementType(@NonNls final String debugName) {
    super(debugName, TheRFileType.INSTANCE.getLanguage());
  }

  public abstract PsiElement createElement(@NotNull final ASTNode node);

  @Override
  public void indexStub(@NotNull final StubT stub, @NotNull final IndexSink sink) {
  }

  @Override
  @NotNull
  public String getExternalId() {
    return "ther." + super.toString();
  }
}
