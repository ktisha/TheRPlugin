package com.jetbrains.ther.xdebugger.mock;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.xdebugger.XSourcePosition;
import org.jetbrains.annotations.NotNull;

public class IllegalXSourcePosition implements XSourcePosition {

  @Override
  public int getLine() {
    throw new IllegalStateException("GetLine shouldn't be called");
  }

  @Override
  public int getOffset() {
    throw new IllegalStateException("GetOffset shouldn't be called");
  }

  @NotNull
  @Override
  public VirtualFile getFile() {
    throw new IllegalStateException("GetFile shouldn't be called");
  }

  @NotNull
  @Override
  public Navigatable createNavigatable(@NotNull final Project project) {
    throw new IllegalStateException("CreateNavigatable shouldn't be called");
  }
}
