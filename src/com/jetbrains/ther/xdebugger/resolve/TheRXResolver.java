package com.jetbrains.ther.xdebugger.resolve;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import org.jetbrains.annotations.NotNull;

// TODO [xdbg][test]
public class TheRXResolver {

  @NotNull
  private final VirtualFile myVirtualFile;

  @NotNull
  private final TheRXFunctionDescriptor myRoot;

  public TheRXResolver(@NotNull final Project project, @NotNull final String scriptPath) {
    myVirtualFile = LocalFileSystem.getInstance().findFileByPath(scriptPath); // TODO [xdbg][null]

    final PsiFile psiFile = PsiManager.getInstance(project).findFile(myVirtualFile); // TODO [xdbg][null]
    final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile); // TODO [xdbg][null]

    final TheRXFunctionDefinitionProcessor processor = new TheRXFunctionDefinitionProcessor(document);

    PsiTreeUtil.processElements(psiFile, processor);

    myRoot = processor.getRoot();
  }

  @NotNull
  public TheRXResolvingSession getSession() {
    return new TheRXResolvingSession(this);
  }

  @NotNull
  TheRXFunctionDescriptor getRoot() {
    return myRoot;
  }

  @NotNull
  XSourcePosition resolve(@NotNull final TheRXFunctionDescriptor descriptor, final int line) {
    final int result = calculateLineOffset(descriptor) + line;

    return XDebuggerUtil.getInstance().createPosition(myVirtualFile, result); // TODO [xdbg][null]
  }

  private int calculateLineOffset(@NotNull final TheRXFunctionDescriptor descriptor) {
    if (descriptor.getParent() == null) {
      return 0;
    }

    return getEldestNotMainParent(descriptor).getStartLine();
  }

  @NotNull
  private TheRXFunctionDescriptor getEldestNotMainParent(@NotNull final TheRXFunctionDescriptor descriptor) {
    TheRXFunctionDescriptor current = descriptor;

    // 1. Method is called with descriptor which holds any function except `MAIN_FUNCTION`.
    // 2. There is only one descriptor which has `null` parent. This descriptor is root and it holds `MAIN_FUNCTION`.
    // Conclusion: current.getParent() can't return `null` here.

    //noinspection ConstantConditions
    while (current.getParent().getParent() != null) {
      current = current.getParent();
    }

    return current;
  }
}
