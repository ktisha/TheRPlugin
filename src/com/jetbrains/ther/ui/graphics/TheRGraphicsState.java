package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;

public interface TheRGraphicsState {

  boolean hasNext();

  boolean hasPrevious();

  void next();

  void previous();

  @NotNull
  VirtualFile current() throws FileNotFoundException;

  int size();

  void refresh(final boolean asynchronous);

  void reset();

  void addListener(@NotNull final Listener listener);

  void removeListener(@NotNull final Listener listener);

  interface Listener {
    void onStarted();

    void onUpdate();

    void onReset();
  }
}
