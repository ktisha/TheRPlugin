package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRScriptLine;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface TheRScriptReader {

  @NotNull
  TheRScriptLine getCurrentLine();

  void advance() throws IOException;

  void close() throws IOException;
}
