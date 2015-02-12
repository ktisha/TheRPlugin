// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRPsiElement;
import com.jetbrains.ther.psi.api.TheRVisitor;
import org.jetbrains.annotations.NotNull;

public class TheRAssignmentStatementImpl extends TheRExpressionImpl implements TheRAssignmentStatement {

  public TheRAssignmentStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitAssignmentStatement(this);
    else super.accept(visitor);
  }

  public boolean isLeft() {
    return TheRPsiImplUtil.isLeft(this);
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
