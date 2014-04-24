package com.jetbrains.ther;

import com.intellij.lang.Language;

public class TheRLanguage extends Language {
  public static TheRLanguage getInstance() {
    return (TheRLanguage)TheRFileType.INSTANCE.getLanguage();
  }

  @Override
  public boolean isCaseSensitive() {
    return true; // http://jetbrains-feed.appspot.com/message/372001
  }

  protected TheRLanguage() {
    super("TheR");
  }
}
