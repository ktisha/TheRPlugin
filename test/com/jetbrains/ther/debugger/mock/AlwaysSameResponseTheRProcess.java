package com.jetbrains.ther.debugger.mock;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
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

  private int myExecuteCalled;

  public AlwaysSameResponseTheRProcess(@NotNull final String text,
                                       @NotNull final TheRProcessResponseType type,
                                       @NotNull final TextRange outputRange,
                                       @NotNull final String error) {
    myText = text;
    myType = type;
    myOutputRange = outputRange;
    myError = error;
    myExecuteCalled = 0;
  }

  @NotNull
  @Override
  public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
    myExecuteCalled++;

    return new TheRProcessResponse(myText, myType, myOutputRange, myError);
  }

  @Override
  public void stop() {
  }

  public int getExecuteCalled() {
    return myExecuteCalled;
  }
}
