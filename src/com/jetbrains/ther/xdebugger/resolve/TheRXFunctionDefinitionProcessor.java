package com.jetbrains.ther.xdebugger.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRFunctionExpression;
import org.jetbrains.annotations.NotNull;

// TODO [xdbg][test]
class TheRXFunctionDefinitionProcessor implements PsiElementProcessor<PsiElement> {

  @NotNull
  private final TheRXFunctionDescriptor myRoot;

  public TheRXFunctionDefinitionProcessor() {
    myRoot = new TheRXFunctionDescriptor(
      TheRDebugConstants.MAIN_FUNCTION.getName(),
      null,
      0,
      Integer.MAX_VALUE
    );
  }

  @Override
  public boolean execute(@NotNull final PsiElement element) {
    if (element instanceof TheRFunctionExpression && element.getParent() instanceof TheRAssignmentStatement) {
      final TheRAssignmentStatement parent = (TheRAssignmentStatement)element.getParent();

      TheRXFunctionDescriptorUtils.add(
        myRoot,
        parent.getName(),
        parent.getTextOffset(),
        parent.getTextOffset() + parent.getTextLength()
      );
    }

    return true;
  }

  @NotNull
  public TheRXFunctionDescriptor getRoot() {
    return myRoot;
  }
}
