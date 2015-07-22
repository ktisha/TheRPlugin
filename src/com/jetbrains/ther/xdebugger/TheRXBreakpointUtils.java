package com.jetbrains.ther.xdebugger;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
import com.intellij.xdebugger.XDebuggerUtil;
import com.jetbrains.ther.TheRFileType;
import com.jetbrains.ther.parsing.TheRElementTypes;
import org.jetbrains.annotations.NotNull;

// TODO [xdbg][test]
final class TheRXBreakpointUtils {

  public static boolean canPutAt(@NotNull final Project project, @NotNull final VirtualFile file, final int line) {
    return isTheRFile(file) && isStoppable(file, line, project);
  }

  private static boolean isTheRFile(@NotNull final VirtualFile file) {
    final String defaultExtension = TheRFileType.INSTANCE.getDefaultExtension();
    final String extension = file.getExtension();

    return defaultExtension.equalsIgnoreCase(extension);
  }

  private static boolean isStoppable(@NotNull final VirtualFile file, final int line, @NotNull final Project project) {
    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file); // TODO [xdbg][null]
    final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile); // TODO [xdbg][null]
    final boolean[] justResult = new boolean[]{false};

    XDebuggerUtil.getInstance().iterateLine(
      project,
      document,
      line,
      new Processor<PsiElement>() {
        @Override
        public boolean process(@NotNull final PsiElement element) {
          if (isNotStoppable(element) || isNotStoppable(element.getNode().getElementType())) return true;

          justResult[0] = true;
          return false;
        }
      }
    );

    return justResult[0];
  }

  private static boolean isNotStoppable(@NotNull final PsiElement element) {
    return element instanceof PsiWhiteSpace || element instanceof PsiComment;
  }

  private static boolean isNotStoppable(@NotNull final IElementType type) {
    return type == TheRElementTypes.THE_R_LBRACE || type == TheRElementTypes.THE_R_RBRACE;
  }
}
