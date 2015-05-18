package com.jetbrains.ther.xdebugger;

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

public class TheRXDebuggerEditorsProvider extends XDebuggerEditorsProvider {

  @NotNull
  private static final String FRAGMENT_NAME = "fragment.r";

  @NotNull
  @Override
  public FileType getFileType() {
    return TheRFileType.INSTANCE;
  }

  @NotNull
  @Override
  public Document createDocument(@NotNull final Project project,
                                 @NotNull final String text,
                                 @Nullable final XSourcePosition sourcePosition,
                                 @NotNull final EvaluationMode mode) {
    final PsiFile psiFile = new TheRCodeFragment(project, FRAGMENT_NAME, text);

    return PsiDocumentManager.getInstance(project).getDocument(psiFile);
  }
}