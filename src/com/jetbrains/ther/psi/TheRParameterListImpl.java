package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.psi.api.TheRParameter;
import com.jetbrains.ther.psi.api.TheRParameterList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TheRParameterListImpl extends TheRElementImpl implements TheRParameterList {
  public TheRParameterListImpl(@NotNull final ASTNode astNode) {
    super(astNode);
  }

  @Override
  public TheRParameter[] getParameters() {
    final ASTNode[] children = getNode().getChildren(TokenSet.create(TheRElementTypes.PARAMETER));
    if (children != null) {
      final ArrayList<TheRParameter> parameters = new ArrayList<TheRParameter>();
      for (ASTNode aChildren : children) {
        parameters.add((TheRParameter)aChildren.getPsi());
      }
      return parameters.toArray(new TheRParameter[parameters.size()]);
    }
    return null;
  }
}
