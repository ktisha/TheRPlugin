package com.jetbrains.ther.psi.stubs;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.StringRef;
import com.jetbrains.ther.psi.TheRAssignmentStatementImpl;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRFunctionExpression;
import com.jetbrains.ther.psi.api.TheRPsiElement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TheRAssignmentElementType extends TheRStubElementType<TheRAssignmentStub, TheRAssignmentStatement> {
  public TheRAssignmentElementType(@NotNull final String debugName) {
    super(debugName);
  }

  @Override
  public PsiElement createElement(@NotNull final ASTNode node) {
    return new TheRAssignmentStatementImpl(node);
  }

  @Override
  public TheRAssignmentStatement createPsi(@NotNull final TheRAssignmentStub stub) {
    return new TheRAssignmentStatementImpl(stub, this);
  }

  @Override
  public TheRAssignmentStub createStub(@NotNull TheRAssignmentStatement psi, StubElement parentStub) {
    final String name = psi.getName();
    final TheRPsiElement value = psi.getAssignedValue();
    return new TheRAssignmentStubImpl(name, parentStub, this, value instanceof TheRFunctionExpression);
  }

  @Override
  public void serialize(@NotNull final TheRAssignmentStub stub, @NotNull final StubOutputStream dataStream)
    throws IOException {
    dataStream.writeName(stub.getName());
    dataStream.writeBoolean(stub.isFunctionDeclaration());
  }

  @Override
  @NotNull
  public TheRAssignmentStub deserialize(@NotNull final StubInputStream dataStream, final StubElement parentStub) throws IOException {
    String name = StringRef.toString(dataStream.readName());
    final boolean isFunctionDefinition = dataStream.readBoolean();
    return new TheRAssignmentStubImpl(name, parentStub, this, isFunctionDefinition);
  }

  @Override
  public void indexStub(@NotNull final TheRAssignmentStub stub, @NotNull final IndexSink sink) {
    final String name = stub.getName();
    if (name != null && stub.getParentStub() instanceof PsiFileStub && stub.isFunctionDeclaration()) {
      sink.occurrence(TheRAssignmentNameIndex.KEY, name);
    }
  }
}
