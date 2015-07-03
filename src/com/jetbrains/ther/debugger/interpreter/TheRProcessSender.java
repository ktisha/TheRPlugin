package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStreamWriter;

class TheRProcessSender {

  @NotNull
  private final OutputStreamWriter myWriter;

  public TheRProcessSender(@NotNull final OutputStreamWriter writer) {
    myWriter = writer;
  }

  public void send(@NotNull final String command) throws IOException {
    myWriter.write(command);
    myWriter.write(TheRDebugConstants.LINE_SEPARATOR);
    myWriter.flush();
  }
}
