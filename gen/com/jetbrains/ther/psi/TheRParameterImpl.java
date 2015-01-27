// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.ther.parsing.TheRElementTypes.*;
import com.jetbrains.ther.psi.api.*;

public class TheRParameterImpl extends TheRElementImpl implements TheRParameter {

  public TheRParameterImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitParameter(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public TheRExpression getExpression() {
    return findChildByClass(TheRExpression.class);
  }

  @Override
  @Nullable
  public PsiElement getEq() {
    return findChildByType(THE_R_EQ);
  }

  @Override
  @Nullable
  public PsiElement getIdentifier() {
    return findChildByType(THE_R_IDENTIFIER);
  }

  public ASTNode getNameNode() {
    return TheRPsiImplUtil.getNameNode(this);
  }

  public String getName() {
    return TheRPsiImplUtil.getName(this);
  }

  public PsiElement setName(String name) {
    return TheRPsiImplUtil.setName(this, name);
  }

}
