package com.jetbrains.ther.run.debug.resolve;

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
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.run.debug.TheRDebugException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

// TODO [xdbg][test]
public class TheRResolvingSessionImpl implements TheRResolvingSession {

  @NotNull
  private final TheRFunctionDescriptor myRoot;

  @NotNull
  private final VirtualFile myVirtualFile;

  @NotNull
  private final List<TheRXResolvingSessionEntry> myEntries;

  public TheRResolvingSessionImpl(@NotNull final Project project, @NotNull final String scriptPath) throws TheRDebugException {
    myVirtualFile = findVirtualFile(scriptPath);
    myRoot = calculateRoot(project, findPsiFile(project, myVirtualFile));
    myEntries = new ArrayList<TheRXResolvingSessionEntry>();
  }

  @Override
  @Nullable
  public XSourcePosition resolveNext(@NotNull final TheRLocation nextLocation) {
    addEntry(nextLocation);

    return resolvePosition(myEntries.get(myEntries.size() - 1));
  }

  @Override
  @Nullable
  public XSourcePosition resolveCurrent(final int line) {
    updateCurrentEntry(line);

    return resolvePosition(myEntries.get(myEntries.size() - 1));
  }

  @Override
  public void dropLast(final int number) {
    final ListIterator<TheRXResolvingSessionEntry> iterator = myEntries.listIterator(myEntries.size());
    iterator.previous();

    for (int i = 0; i < number; i++) {
      iterator.remove();
      iterator.previous();
    }
  }

  @NotNull
  private VirtualFile findVirtualFile(@NotNull final String scriptPath) throws TheRDebugException {
    final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(scriptPath);

    if (virtualFile == null) throw new TheRDebugException(scriptPath + " is not found");

    return virtualFile;
  }

  @NotNull
  private PsiFile findPsiFile(@NotNull final Project project, @NotNull final VirtualFile virtualFile) throws TheRDebugException {
    final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

    if (psiFile == null) throw new TheRDebugException(virtualFile.getName() + " couldn't be loaded");

    return psiFile;
  }

  @NotNull
  private TheRFunctionDescriptor calculateRoot(@NotNull final Project project, @NotNull final PsiFile psiFile)
    throws TheRDebugException {
    final TheRFunctionDefinitionProcessor processor = new TheRFunctionDefinitionProcessor(findDocument(project, psiFile));

    PsiTreeUtil.processElements(psiFile, processor);

    return processor.getRoot();
  }

  @NotNull
  private Document findDocument(@NotNull final Project project, @NotNull final PsiFile psiFile) throws TheRDebugException {
    final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);

    if (document == null) throw new TheRDebugException(psiFile.getName() + " couldn't be loaded");

    return document;
  }

  private void addEntry(@NotNull final TheRLocation nextLocation) {
    final String nextFunctionName = nextLocation.getFunctionName();
    final TheRFunctionDescriptor descriptor = myEntries.isEmpty()
                                               ? myRoot
                                               : resolveDescriptor(myEntries.listIterator(myEntries.size()), nextFunctionName);


    myEntries.add(
      new TheRXResolvingSessionEntry(
        descriptor,
        resolveLine(descriptor, nextLocation.getLine())
      )
    );
  }

  @Nullable
  private XSourcePosition resolvePosition(@NotNull final TheRXResolvingSessionEntry entry) {
    if (entry.myDescriptor == null) {
      return null;
    }

    return XDebuggerUtil.getInstance().createPosition(myVirtualFile, entry.myLine);
  }

  private void updateCurrentEntry(final int line) {
    final int lastIndex = myEntries.size() - 1;
    final TheRFunctionDescriptor descriptor = myEntries.get(lastIndex).myDescriptor;

    myEntries.set(
      lastIndex,
      new TheRXResolvingSessionEntry(
        descriptor,
        resolveLine(descriptor, line)
      )
    );
  }

  @Nullable
  private TheRFunctionDescriptor resolveDescriptor(@NotNull final ListIterator<TheRXResolvingSessionEntry> entries,
                                                   @NotNull final String nextFunctionName) {
    if (!entries.hasPrevious()) {
      return null;
    }

    final TheRFunctionDescriptor candidate = resolveDescriptor(entries.previous(), nextFunctionName);

    if (candidate != null) {
      return candidate;
    }

    return resolveDescriptor(entries, nextFunctionName);
  }

  private int resolveLine(@Nullable final TheRFunctionDescriptor descriptor, final int line) {
    final boolean isUnbraceFunction = descriptor != null && line == 0;

    return isUnbraceFunction
           ? descriptor.getStartLine()
           : line - 1; // convert 1-based to 0-based
  }

  @Nullable
  private TheRFunctionDescriptor resolveDescriptor(@NotNull final TheRXResolvingSessionEntry entry,
                                                   @NotNull final String nextFunctionName) {
    final TheRFunctionDescriptor currentDescriptor = entry.myDescriptor;

    if (currentDescriptor == null) {
      return null;
    }

    TheRFunctionDescriptor result = null;

    if (currentDescriptor.getChildren().containsKey(nextFunctionName)) {
      int distance = Integer.MAX_VALUE;

      for (final TheRFunctionDescriptor candidate : currentDescriptor.getChildren().get(nextFunctionName)) {
        final int currentDistance = currentDescriptor.getStartLine() + entry.myLine - candidate.getStartLine();

        if (currentDistance > 0 && currentDistance < distance) { // candidate is declared before the current line
          result = candidate;
          distance = currentDistance;
        }
      }
    }

    return result;
  }

  private static class TheRXResolvingSessionEntry {

    @Nullable
    private final TheRFunctionDescriptor myDescriptor;

    private final int myLine;

    public TheRXResolvingSessionEntry(@Nullable final TheRFunctionDescriptor descriptor, final int line) {
      myDescriptor = descriptor;
      myLine = line;
    }
  }
}
