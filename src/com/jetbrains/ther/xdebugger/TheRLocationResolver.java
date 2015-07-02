package com.jetbrains.ther.xdebugger;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRFunctionExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

// TODO [xdbg][test]
public class TheRLocationResolver {

  @NotNull
  private final VirtualFile myVirtualFile;

  @NotNull
  private final Document myDocument;

  @NotNull
  private final Map<String, Integer> myFunctionLine;

  public TheRLocationResolver(@NotNull final Project project, @NotNull final String scriptPath) {
    myVirtualFile = LocalFileSystem.getInstance().findFileByPath(scriptPath);

    final PsiFile psiFile = PsiManager.getInstance(project).findFile(myVirtualFile);
    myDocument = PsiDocumentManager.getInstance(project).getDocument(psiFile);

    myFunctionLine = new HashMap<String, Integer>();

    PsiTreeUtil.processElements(psiFile, new FunctionDefinitionProcessor());
  }

  @NotNull
  public XSourcePosition resolve(@NotNull final TheRLocation location) {
    return XDebuggerUtil.getInstance().createPosition(myVirtualFile, calculateFunctionLine(location) + location.getLine());
  }

  private int calculateFunctionLine(@NotNull final TheRLocation location) {
    if (location.getFunction().equals(TheRDebugConstants.MAIN_FUNCTION_NAME)) {
      return 0;
    }

    final Integer offset = myFunctionLine.get(location.getFunction());

    if (offset == null) {
      return 0; // TODO [xdbg][update]
    }
    else {
      return offset;
    }
  }

  private class FunctionDefinitionProcessor implements PsiElementProcessor<PsiElement> {

    @Override
    public boolean execute(@NotNull final PsiElement element) {
      if (element instanceof TheRFunctionExpression && element.getParent() instanceof TheRAssignmentStatement) {
        final String name = ((TheRAssignmentStatement)element.getParent()).getName();
        final int line = myDocument.getLineNumber(element.getTextOffset());

        myFunctionLine.put(name, line);
      }

      return true;
    }
  }
}
