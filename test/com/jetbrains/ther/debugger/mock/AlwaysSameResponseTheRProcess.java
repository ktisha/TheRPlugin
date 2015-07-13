package com.jetbrains.ther.debugger.mock;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AlwaysSameResponseTheRProcess extends TheRProcess {

  @NotNull
  private final String myText;

  @NotNull
  private final TheRProcessResponseType myType;

  @NotNull
  private final TextRange myOutputRange;

  public AlwaysSameResponseTheRProcess(@NotNull final String text,
                                       @NotNull final TheRProcessResponseType type,
                                       @NotNull final TextRange outputRange) {
    myText = text;
    myType = type;
    myOutputRange = outputRange;
  }

  @NotNull
  @Override
  public TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException {
    return new TheRProcessResponse(myText, myType, myOutputRange);
  }

  @Override
  public void stop() {
  }
}
