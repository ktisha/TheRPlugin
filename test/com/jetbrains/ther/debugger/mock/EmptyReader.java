package com.jetbrains.ther.debugger.mock;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;

public class EmptyReader extends Reader {

  @Override
  public int read(@NotNull final char[] cbuf, final int off, final int len) throws IOException {
    return -1;
  }

  @Override
  public void close() throws IOException {
  }
}
