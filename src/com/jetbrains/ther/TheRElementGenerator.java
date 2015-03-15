package com.jetbrains.ther;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.testFramework.LightVirtualFile;

public class TheRElementGenerator {

  public static PsiFile createDummyFile(String contents, boolean physical, Project project) {
    final PsiFileFactory factory = PsiFileFactory.getInstance(project);
    final String name = "dummy." + TheRFileType.INSTANCE.getDefaultExtension();
    final LightVirtualFile virtualFile = new LightVirtualFile(name, TheRFileType.INSTANCE, contents);
    final PsiFile psiFile = ((PsiFileFactoryImpl)factory).trySetupPsiForFile(virtualFile, TheRLanguage.getInstance(), physical, true);
    assert psiFile != null;
    return psiFile;
  }
}
