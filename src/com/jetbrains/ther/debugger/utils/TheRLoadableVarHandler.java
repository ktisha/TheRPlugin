package com.jetbrains.ther.debugger.utils;

import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface TheRLoadableVarHandler {

  @Nullable
  String handleType(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type)
    throws IOException, InterruptedException;

  @NotNull
  String handleValue(@NotNull final String var,
                     @NotNull final String type,
                     @NotNull final String value);
}
