package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class IllegalTheRLoadableVarHandler implements TheRLoadableVarHandler {

  @Nullable
  @Override
  public String handleType(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type)
    throws IOException, InterruptedException {
    throw new IllegalStateException("HandleType shouldn't be called");
  }

  @NotNull
  @Override
  public String handleValue(@NotNull final String var,
                            @NotNull final String type,
                            @NotNull final String value) {
    throw new IllegalStateException("HandleValue shouldn't be called");
  }
}
