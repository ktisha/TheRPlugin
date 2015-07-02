package com.jetbrains.ther.xdebugger;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.ther.TheRFileType;
import com.jetbrains.ther.debugger.TheRDebugUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO [xdbg][test]
public class TheRXLineBreakpointType extends XLineBreakpointType<XBreakpointProperties> {

  @NotNull
  private static final String ID = "the-r-line";

  @NotNull
  private static final String TITLE = "The R breakpoints";

  @NotNull
  private final TheRXDebuggerEditorsProvider myProvider = new TheRXDebuggerEditorsProvider();

  public TheRXLineBreakpointType() {
    super(ID, TITLE);
  }

  @Nullable
  @Override
  public XBreakpointProperties createBreakpointProperties(@NotNull final VirtualFile file, final int line) {
    return null;
  }

  @Override
  public boolean canPutAt(@NotNull final VirtualFile file, final int line, @NotNull final Project project) {
    return isTheRFile(file) && !isCommentOrSpaces(file, line, project);
  }

  @Nullable
  @Override
  public XDebuggerEditorsProvider getEditorsProvider(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint,
                                                     @NotNull final Project project) {
    return myProvider;
  }

  private boolean isTheRFile(final @NotNull VirtualFile file) {
    final String defaultExtension = TheRFileType.INSTANCE.getDefaultExtension();
    final String extension = file.getExtension();

    return defaultExtension.equalsIgnoreCase(extension);
  }

  private boolean isCommentOrSpaces(final @NotNull VirtualFile file, final int line, final @NotNull Project project) {
    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);

    final String text = document.getText(
      new TextRange(
        document.getLineStartOffset(line),
        document.getLineEndOffset(line)
      )
    );

    return TheRDebugUtils.isCommentOrSpaces(text);
  }
}
