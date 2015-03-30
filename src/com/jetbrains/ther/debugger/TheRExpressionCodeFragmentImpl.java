package com.jetbrains.ther.debugger;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.ther.psi.TheRFileImpl;
import org.jetbrains.annotations.NotNull;

public class TheRExpressionCodeFragmentImpl extends TheRFileImpl {

  public TheRExpressionCodeFragmentImpl(@NotNull Project project, @NotNull String name, @NotNull String text) {
    super(((PsiManagerEx)PsiManager.getInstance(project)).getFileManager().createFileViewProvider(
            new LightVirtualFile(name, FileTypeManager.getInstance().getFileTypeByFileName(name), text), true
          )
    );

    ((SingleRootFileViewProvider)getViewProvider()).forceCachedPsi(this);
  }
}
