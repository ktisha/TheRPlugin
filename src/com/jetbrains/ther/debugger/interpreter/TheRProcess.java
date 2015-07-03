package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface TheRProcess {

  @NotNull
  TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException;

  void stop();
}
