package com.jetbrains.ther;

import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TheRFileType extends LanguageFileType {
  public static final TheRFileType INSTANCE = new TheRFileType();

  public TheRFileType() {
    this(new TheRLanguage());
  }

  public TheRFileType(@NotNull final Language language) {
    super(language);
  }

  @Override
  @NotNull
  public String getName() {
    return "The R";
  }

  @Override
  @NotNull
  public String getDescription() {
    return "The R files";
  }

  @Override
  @NotNull
  public String getDefaultExtension() {
    return "r";
  }

  @Override
  @NotNull
  public Icon getIcon() {
    return AllIcons.FileTypes.Text;    // TODO: create an icon
  }
}
