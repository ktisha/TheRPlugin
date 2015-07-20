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
import com.jetbrains.ther.debugger.TheRFunctionResolver;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRFunction;
import com.jetbrains.ther.debugger.data.TheRLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO [xdbg][test]
public class TheRXResolver implements TheRFunctionResolver {

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
  public XSourcePosition resolve(@NotNull final TheRLocation location) {
    final int line = calculateLineOffset(location) + location.getLine();

    return XDebuggerUtil.getInstance().createPosition(myVirtualFile, line); // TODO [xdbg][null]
  }

  @NotNull
  @Override
  public TheRFunction resolve(@NotNull final TheRLocation currentLocation, @NotNull final String nextFunctionName) {
    final TheRXFunctionDescriptor nextDescriptor =
      TheRXFunctionDescriptorUtils.resolveNext(myRoot, currentLocation, nextFunctionName); // TODO [xdbg][null]

    return new TheRFunction(calculateDefinition(nextDescriptor));
  }

  private int calculateLineOffset(@NotNull final TheRLocation location) {
    if (location.getFunction().equals(TheRDebugConstants.MAIN_FUNCTION)) {
      return 0;
    }

    final TheRXFunctionDescriptor current = TheRXFunctionDescriptorUtils.resolveCurrent(myRoot, location);  // TODO [xdbg][null]

    return getEldestNotMainParent(current).getStartLine();
  }

  @NotNull
  private TheRXFunctionDescriptor getEldestNotMainParent(@NotNull final TheRXFunctionDescriptor descriptor) {
    TheRXFunctionDescriptor current = descriptor;

    // 1. Method is called with descriptor which holds any function except `MAIN_FUNCTION`.
    // 2. There is only one descriptor which has `null` parent. This descriptor is root and it holds `MAIN_FUNCTION`.
    // Conclusion: getParent() can't return `null` here.

    //noinspection ConstantConditions
    while (!current.getParent().getName().equals(TheRDebugConstants.MAIN_FUNCTION.getName())) {
      current = current.getParent();
    }

    return current;
  }

  @NotNull
  private List<String> calculateDefinition(@NotNull final TheRXFunctionDescriptor descriptor) {
    final List<String> result = new ArrayList<String>();

    TheRXFunctionDescriptor current = descriptor;

    while (current != null) {
      result.add(current.getName());

      current = current.getParent();
    }

    Collections.reverse(result);

    return result;
  }
}
