// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.ther.parsing.TheRElementTypes.*;
import com.jetbrains.ther.psi.stubs.TheRAssignmentBase;
import com.jetbrains.ther.psi.api.*;
import com.intellij.psi.stubs.IStubElementType;

public class TheRAssignmentStatementImpl extends TheRAssignmentBase implements TheRAssignmentStatement {

  public TheRAssignmentStatementImpl(ASTNode node) {
    super(node);
  }

  public TheRAssignmentStatementImpl(com.jetbrains.ther.psi.stubs.TheRAssignmentStub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitAssignmentStatement(this);
    else super.accept(visitor);
  }

  public boolean isLeft() {
    return TheRPsiImplUtil.isLeft(this);
  }

  public boolean isRight() {
    return TheRPsiImplUtil.isRight(this);
  }

  public TheRPsiElement getAssignedValue() {
    return TheRPsiImplUtil.getAssignedValue(this);
  }

  public PsiElement getAssignee() {
    return TheRPsiImplUtil.getAssignee(this);
  }

  public String getName() {
    return TheRPsiImplUtil.getName(this);
  }

  public PsiElement setName(String name) {
    return TheRPsiImplUtil.setName(this, name);
  }

  public ASTNode getNameNode() {
    return TheRPsiImplUtil.getNameNode(this);
  }

}
