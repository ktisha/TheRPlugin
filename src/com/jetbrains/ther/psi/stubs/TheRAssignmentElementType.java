package com.jetbrains.ther.psi.stubs;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.StringRef;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.psi.TheRAssignmentStatementImpl;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TheRAssignmentElementType extends TheRStubElementType<TheRAssignmentStub, TheRAssignmentStatement> {
  public TheRAssignmentElementType() {
    this("ASSIGNMENT");
  }

  public TheRAssignmentElementType(@NotNull final String debugName) {
    super(debugName);
  }

  @Override
  public PsiElement createElement(@NotNull final ASTNode node) {
    return new TheRAssignmentStatementImpl(node);
  }

  @Override
  public TheRAssignmentStatement createPsi(@NotNull final TheRAssignmentStub stub) {
    return new TheRAssignmentStatementImpl(stub);
  }

  @Override
  public TheRAssignmentStub createStub(@NotNull TheRAssignmentStatement psi, StubElement parentStub) {
    final String name = psi.getName();
    return new TheRAssignmentStubImpl(name, parentStub, getStubElementType());
  }

  @Override
  public void serialize(@NotNull final TheRAssignmentStub stub, @NotNull final StubOutputStream dataStream)
      throws IOException {
    dataStream.writeName(stub.getName());
  }

  @Override
  @NotNull
  public TheRAssignmentStub deserialize(@NotNull final StubInputStream dataStream, final StubElement parentStub) throws IOException {
    String name = StringRef.toString(dataStream.readName());
    return new TheRAssignmentStubImpl(name, parentStub, getStubElementType());
  }

  @Override
  public void indexStub(@NotNull final TheRAssignmentStub stub, @NotNull final IndexSink sink) {
    final String name = stub.getName();
    if (name != null) {
      sink.occurrence(TheRAssignmentNameIndex.KEY, name);
    }
  }

  protected IStubElementType getStubElementType() {
    return TheRElementTypes.ASSIGNMENT_STATEMENT;
  }
}
