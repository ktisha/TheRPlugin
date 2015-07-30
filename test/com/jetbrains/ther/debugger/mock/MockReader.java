package com.jetbrains.ther.debugger.mock;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;

public class MockReader extends Reader {

  @NotNull
  private final String myText;

  private int myCounter;

  public MockReader(@NotNull final String text) {
    myText = text;
    myCounter = 0;
  }

  @Override
  public boolean ready() throws IOException {
    return !myText.isEmpty() && myCounter == 0;
  }

  @Override
  public int read(@NotNull final char[] cbuf, final int off, final int len) throws IOException {
    if (myText.isEmpty()) {
      return -1;
    }

    for (int index = 0; index < myText.length(); index++) {
      cbuf[index] = myText.charAt(index);
    }

    myCounter++;

    return myText.length();
  }

  @Override
  public void close() throws IOException {
  }

  public int getCounter() {
    return myCounter;
  }
}
