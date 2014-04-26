package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.ther.TheRFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class TheRElementType extends IElementType {
  protected Class<? extends PsiElement> myPsiElementClass;
  private Constructor<? extends PsiElement> myConstructor;
  private static final Class[] PARAMETER_TYPES = new Class[]{ASTNode.class};

  public TheRElementType(@NotNull @NonNls final String debugName) {
    super(debugName, TheRFileType.INSTANCE.getLanguage());
  }

  public TheRElementType(@NotNull @NonNls final String debugName, @NotNull final Class<? extends PsiElement> psiElementClass) {
    super(debugName, TheRFileType.INSTANCE.getLanguage());
    myPsiElementClass = psiElementClass;
  }

  @Override
  public String toString() {
    return "TheR:" + super.toString();
  }

  @NotNull
  public PsiElement createElement(@NotNull final ASTNode node) {
    if (myPsiElementClass == null) {
      throw new IllegalStateException("Cannot create an element for " + node.getElementType() + " without element class");
    }
    try {
      if (myConstructor == null) {
        myConstructor = myPsiElementClass.getConstructor(PARAMETER_TYPES);
      }
      return myConstructor.newInstance(node);
    }
    catch (Exception e) {
      throw new IllegalStateException("No necessary constructor for " + node.getElementType(), e);
    }
  }

}
