package com.jetbrains.ther.xdebugger;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.impl.XDebuggerUtilImpl;
import com.jetbrains.ther.debugger.data.TheRLocation;
import org.jetbrains.annotations.NotNull;

public class TheRLocationResolver {

  @NotNull
  public XSourcePosition resolve(@NotNull final TheRLocation location) { // TODO update
    final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath("temp.r"); // TODO update

    return new XDebuggerUtilImpl().createPosition(virtualFile, location.getLine());
  }
}
