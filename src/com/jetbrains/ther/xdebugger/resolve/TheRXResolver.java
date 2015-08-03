package com.jetbrains.ther.xdebugger.resolve;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

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
  public TheRXResolvingSession createSession() {
    return new TheRXResolvingSession(myRoot, myVirtualFile);
  }
}
