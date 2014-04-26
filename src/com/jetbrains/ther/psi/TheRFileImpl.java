package com.jetbrains.ther.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.ther.TheRFileType;
import com.jetbrains.ther.psi.api.TheRFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TheRFileImpl extends PsiFileBase implements TheRFile {

  public TheRFileImpl(@NotNull final FileViewProvider viewProvider) {
    this(viewProvider, TheRFileType.INSTANCE.getLanguage());
  }

  public TheRFileImpl(@NotNull final FileViewProvider viewProvider, @NotNull final Language language) {
    super(viewProvider, language);
  }

  @Override
  @NotNull
  public FileType getFileType() {
    return TheRFileType.INSTANCE;
  }

  public String toString() {
    return "TheRFile:" + getName();
  }

  @Override
  public Icon getIcon(int flags) {
    return AllIcons.FileTypes.Text;   // TODO: icon
  }

  @Override
  public void accept(@NotNull final PsiElementVisitor visitor) {
  }
}
