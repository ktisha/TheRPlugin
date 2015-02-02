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

public class TheRParameterListImpl extends TheRElementImpl implements TheRParameterList {

  public TheRParameterListImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TheRVisitor) ((TheRVisitor)visitor).visitParameterList(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<TheRParameter> getParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TheRParameter.class);
  }

  @Override
  @NotNull
  public PsiElement getLpar() {
    return findNotNullChildByType(THE_R_LPAR);
  }

  @Override
  @NotNull
  public PsiElement getRpar() {
    return findNotNullChildByType(THE_R_RPAR);
  }

}
