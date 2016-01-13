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

  boolean isSnapshot(@NotNull final VirtualFile file);

  void add(@NotNull final VirtualFile file);

  void remove(@NotNull final VirtualFile file);

  void reset();
}
