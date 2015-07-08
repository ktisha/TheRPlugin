package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class TheRProcess {

  @NotNull
  public abstract TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException;

  public abstract void stop();

  @NotNull
  public String execute(@NotNull final String command,
                        @NotNull final TheRProcessResponseType expectedType) throws IOException, InterruptedException {
    final TheRProcessResponse response = execute(command);

    if (response.getType() != expectedType) {
      throw new IOException(
        "Actual response type is not the same as expected: [actual: " + response.getType() + ", expected: " + expectedType + "]"
      );
    }

    return response.getText();
  }
}
