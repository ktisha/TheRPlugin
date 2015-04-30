package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class TheRProcessSender {

  @NotNull
  private final OutputStreamWriter myWriter;

  public TheRProcessSender(@NotNull final OutputStream stream) {
    myWriter = new OutputStreamWriter(stream);
  }

  public void send(@NotNull final String command) throws IOException {
    myWriter.write(command);
    myWriter.write(TheRDebugConstants.LINE_SEPARATOR);
    myWriter.flush();
  }

  public void send(final char command) throws IOException {
    myWriter.write(command);
    myWriter.write(TheRDebugConstants.LINE_SEPARATOR);
    myWriter.flush();
  }
}
