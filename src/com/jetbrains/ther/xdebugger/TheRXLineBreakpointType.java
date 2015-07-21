package com.jetbrains.ther.xdebugger;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.ther.TheRFileType;
import com.jetbrains.ther.parsing.TheRElementTypes;
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
    return isTheRFile(file) && isStoppable(file, line, project);
  }

  @Nullable
  @Override
  public XDebuggerEditorsProvider getEditorsProvider(@NotNull final XLineBreakpoint<XBreakpointProperties> breakpoint,
                                                     @NotNull final Project project) {
    return myProvider;
  }

  private boolean isTheRFile(@NotNull final VirtualFile file) {
    final String defaultExtension = TheRFileType.INSTANCE.getDefaultExtension();
    final String extension = file.getExtension();

    return defaultExtension.equalsIgnoreCase(extension);
  }

  private boolean isStoppable(@NotNull final VirtualFile file, final int line, @NotNull final Project project) {
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

  private boolean isNotStoppable(@NotNull final PsiElement element) {
    return element instanceof PsiWhiteSpace || element instanceof PsiComment;
  }

  private boolean isNotStoppable(@NotNull final IElementType type) {
    return type == TheRElementTypes.THE_R_LBRACE || type == TheRElementTypes.THE_R_RBRACE;
  }
}
