package com.jetbrains.ther.debugger.mock;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;

public class AlwaysSameResponseTheRProcess extends MockTheRProcess {

  @NotNull
  private final String myText;

  @NotNull
  private final TheRProcessResponseType myType;

  @NotNull
  private final TextRange myOutputRange;

  @NotNull
  private final String myError;

  public AlwaysSameResponseTheRProcess(@NotNull final String text,
                                       @NotNull final TheRProcessResponseType type,
                                       @NotNull final TextRange outputRange,
                                       @NotNull final String error) {
    myText = text;
    myType = type;
    myOutputRange = outputRange;
    myError = error;
  }

  @NotNull
  @Override
  protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
    return new TheRProcessResponse(myText, myType, myOutputRange, myError);
  }
}
