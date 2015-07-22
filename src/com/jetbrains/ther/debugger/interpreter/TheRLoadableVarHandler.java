package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TheRLoadableVarHandler {

  @Nullable
  String handleType(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type) throws TheRDebuggerException;

  @NotNull
  String handleValue(@NotNull final String var,
                     @NotNull final String type,
                     @NotNull final String value);
}
