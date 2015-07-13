package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class IllegalTheRProcess extends TheRProcess {

  @NotNull
  @Override
  public TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException {
    throw new IllegalStateException("Execute shouldn't be called");
  }

  @Override
  public void stop() {
  }
}
