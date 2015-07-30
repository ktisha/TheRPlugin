package com.jetbrains.ther.debugger.mock;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;

public class AlwaysSameResponseTheRProcess implements TheRProcess {

  @NotNull
  private final String myText;

  @NotNull
  private final TheRProcessResponseType myType;

  @NotNull
  private final TextRange myOutputRange;

  @NotNull
  private final String myError;

  private int myCounter;

  public AlwaysSameResponseTheRProcess(@NotNull final String text,
                                       @NotNull final TheRProcessResponseType type,
                                       @NotNull final TextRange outputRange,
                                       @NotNull final String error) {
    myText = text;
    myType = type;
    myOutputRange = outputRange;
    myError = error;
    myCounter = 0;
  }

  @NotNull
  @Override
  public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
    myCounter++;

    return new TheRProcessResponse(myText, myType, myOutputRange, myError);
  }

  @Override
  public void stop() {
  }

  public int getCounter() {
    return myCounter;
  }
}
