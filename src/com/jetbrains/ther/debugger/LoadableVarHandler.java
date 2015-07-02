package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface LoadableVarHandler {

  @Nullable
  String handleType(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type)
    throws IOException, InterruptedException;

  @NotNull
  String handleValue(@NotNull final TheRProcess process,
                     @NotNull final String name,
                     @NotNull final String type,
                     @NotNull final String value);
}
