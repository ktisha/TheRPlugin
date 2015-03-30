package com.jetbrains.ther.debugger;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.ther.TheRFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRDebuggerEditorsProvider extends XDebuggerEditorsProvider {

  @NotNull
  @Override
  public FileType getFileType() {
    return TheRFileType.INSTANCE;
  }

  @NotNull
  @Override
  public Document createDocument(@NotNull Project project,
                                 @NotNull String text,
                                 @Nullable XSourcePosition sourcePosition,
                                 @NotNull EvaluationMode mode) {
    PsiFile psiFile = new TheRExpressionCodeFragmentImpl(project, "fragment.r", text);

    return PsiDocumentManager.getInstance(project).getDocument(psiFile);
  }
}
