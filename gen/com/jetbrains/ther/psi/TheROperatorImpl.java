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
import com.jetbrains.ther.psi.references.TheROperatorReference;

public class TheROperatorImpl extends TheRElementImpl implements TheROperator {

  public TheROperatorImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitOperator(this);
    else super.accept(visitor);
  }

  public String getName() {
    return TheRPsiImplUtil.getName(this);
  }

  public TheROperatorReference getReference() {
    return TheRPsiImplUtil.getReference(this);
  }

}
