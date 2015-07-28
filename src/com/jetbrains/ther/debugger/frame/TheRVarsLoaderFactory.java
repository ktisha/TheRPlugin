package com.jetbrains.ther.debugger.frame;

import org.jetbrains.annotations.NotNull;

public interface TheRVarsLoaderFactory {

  @NotNull
  TheRVarsLoader getLoader(final int frameNumber);
}
