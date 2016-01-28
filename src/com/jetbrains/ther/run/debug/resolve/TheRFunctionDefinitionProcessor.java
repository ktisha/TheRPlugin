package com.jetbrains.ther.run.debug.resolve;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRFunctionExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.MAIN_FUNCTION_NAME;

class TheRFunctionDefinitionProcessor implements PsiElementProcessor<PsiElement> {

  @NotNull
  private final Document myDocument;

  @NotNull
  private final TheRFunctionDescriptor myRoot;

  public TheRFunctionDefinitionProcessor(@NotNull final Document document) {
    myDocument = document;

    myRoot = new TheRFunctionDescriptor(
      MAIN_FUNCTION_NAME,
      null,
      0,
      Integer.MAX_VALUE
    );
  }

  @Override
  public boolean execute(@NotNull final PsiElement element) {
    if (element instanceof TheRFunctionExpression && element.getParent() instanceof TheRAssignmentStatement) {
      final TheRAssignmentStatement parent = (TheRAssignmentStatement)element.getParent();
      final int startOffset = parent.getTextOffset();

      // `TheRAssignmentStatement` couldn't be without name
      assert parent.getName() != null;

      add(
        myRoot,
        parent.getName(),
        myDocument.getLineNumber(startOffset),
        myDocument.getLineNumber(startOffset + parent.getTextLength())
      );
    }

    return true;
  }

  @NotNull
  public TheRFunctionDescriptor getRoot() {
    return myRoot;
  }

  private void add(@NotNull final TheRFunctionDescriptor currentDescriptor,
                   @NotNull final String name,
                   final int startLine,
                   final int endLine) {
    if (!trySiftDown(currentDescriptor, name, startLine, endLine)) {
      addAsChild(currentDescriptor, name, startLine, endLine);
    }
  }

  private boolean trySiftDown(@NotNull final TheRFunctionDescriptor currentDescriptor,
                              @NotNull final String name,
                              final int startLine,
                              final int endLine) {
    for (final List<TheRFunctionDescriptor> sameNameChildren : currentDescriptor.getChildren().values()) {
      for (final TheRFunctionDescriptor child : sameNameChildren) {
        if (child.getStartLine() <= startLine && endLine <= child.getEndLine()) {
          add(
            child,
            name,
            startLine,
            endLine
          );

          return true;
        }
      }
    }

    return false;
  }

  private void addAsChild(@NotNull final TheRFunctionDescriptor currentDescriptor,
                          @NotNull final String name,
                          final int startLine,
                          final int endLine) {
    final Map<String, List<TheRFunctionDescriptor>> children = currentDescriptor.getChildren();

    if (!children.containsKey(name)) {
      children.put(
        name,
        new ArrayList<TheRFunctionDescriptor>()
      );
    }

    children.get(name).add(
      new TheRFunctionDescriptor(
        name,
        currentDescriptor,
        startLine,
        endLine
      )
    );
  }
}
