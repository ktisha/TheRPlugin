package com.jetbrains.ther.xdebugger.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
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

import java.util.LinkedList;
import java.util.List;

// TODO [xdbg][test][update]
public class TheRXResolver implements TheRFunctionResolver {

  @NotNull
  private final VirtualFile myVirtualFile;

  @NotNull
  private final TheRXFunctionDescriptor myRoot;

  public TheRXResolver(@NotNull final Project project, @NotNull final String scriptPath) {
    myVirtualFile = LocalFileSystem.getInstance().findFileByPath(scriptPath);

    final PsiFile psiFile = PsiManager.getInstance(project).findFile(myVirtualFile);
    final TheRXFunctionDefinitionProcessor processor = new TheRXFunctionDefinitionProcessor();

    PsiTreeUtil.processElements(psiFile, processor);

    myRoot = processor.getRoot();
  }

  @NotNull
  public XSourcePosition resolve(@NotNull final TheRLocation location) {
    return XDebuggerUtil.getInstance().createPosition(myVirtualFile, calculateFunctionLine(location) + location.getLine());
  }

  @NotNull
  @Override
  public TheRFunction resolve(@NotNull final TheRLocation currentLocation, @NotNull final String nextFunctionName) {
    final TheRXFunctionDescriptor nextDescriptor = TheRXFunctionDescriptorUtils.resolveNext(myRoot, currentLocation, nextFunctionName);

    return new TheRFunction(calculateDefinition(nextDescriptor));
  }

  private int calculateFunctionLine(@NotNull final TheRLocation location) {
    if (location.getFunction().equals(TheRDebugConstants.MAIN_FUNCTION)) {
      return 0;
    }

    final TheRXFunctionDescriptor descriptor = TheRXFunctionDescriptorUtils.resolveCurrent(myRoot, location);

    return descriptor.getStartLine() - highestParentStartLine(descriptor);
  }

  @NotNull
  private List<String> calculateDefinition(@NotNull final TheRXFunctionDescriptor descriptor) {
    final LinkedList<String> result = new LinkedList<String>();

    TheRXFunctionDescriptor current = descriptor;

    while (current != myRoot) {
      result.addFirst(current.getName());

      current = current.getParent();
    }

    return result;
  }

  private int highestParentStartLine(@NotNull final TheRXFunctionDescriptor descriptor) {
    if (descriptor.getParent() == myRoot) {
      return 0;
    }

    TheRXFunctionDescriptor current = descriptor;

    while (current.getParent() != myRoot) {
      current = current.getParent();
    }

    return current.getStartLine();
  }
}
