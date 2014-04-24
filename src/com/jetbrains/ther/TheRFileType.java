package com.jetbrains.ther;

import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TheRFileType extends LanguageFileType {
  public static TheRFileType INSTANCE = new TheRFileType();

  public TheRFileType() {
    this(new TheRLanguage());
  }

  public TheRFileType(Language language) {
    super(language);
  }

  @NotNull
  public String getName() {
    return "The R";
  }

  @NotNull
  public String getDescription() {
    return "The R files";
  }

  @NotNull
  public String getDefaultExtension() {
    return "r";
  }

  @NotNull
  public Icon getIcon() {
    return AllIcons.FileTypes.Text;    // TODO: create an icon
  }
}
