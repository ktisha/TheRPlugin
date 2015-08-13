package com.jetbrains.ther.debugger.frame;

import org.jetbrains.annotations.NotNull;

public interface TheRVarsLoaderFactory {

  @NotNull
  TheRVarsLoader getLoader(@NotNull final TheRValueModifier modifier, final int frameNumber);
}
