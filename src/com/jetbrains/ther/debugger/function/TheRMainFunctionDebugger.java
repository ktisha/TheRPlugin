package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.TheRScriptReader;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRScriptLine;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.UnexpectedResponseException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.*;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.traceAndDebugFunctions;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.*;

// TODO [dbg][test]
class TheRMainFunctionDebugger implements TheRFunctionDebugger {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerFactory myDebuggerFactory;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheROutputReceiver myOutputReceiver;

  @NotNull
  private final TheRScriptReader myScriptReader;

  private boolean myIsRunning;
  private boolean myIsNewDebuggerAppended;

  public TheRMainFunctionDebugger(@NotNull final TheRProcess process,
                                  @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                  @NotNull final TheROutputReceiver outputReceiver,
                                  @NotNull final TheRScriptReader scriptReader) {
    myProcess = process;
    myDebuggerFactory = debuggerFactory;
    myDebuggerHandler = debuggerHandler;
    myOutputReceiver = outputReceiver;
    myScriptReader = scriptReader;

    myIsRunning = false;
    myIsNewDebuggerAppended = false;
  }

  @NotNull
  @Override
  public TheRLocation getLocation() {
    return new TheRLocation(
      TheRDebugConstants.MAIN_FUNCTION_NAME,
      getCurrentLineNumber()
    );
  }

  @Override
  public boolean hasNext() {
    return !myIsRunning || getCurrentLineNumber() != -1;
  }

  public void advance() throws TheRDebuggerException {
    if (!hasNext()) {
      throw new IllegalStateException("Advance should be called only if hasNext returns true");
    }

    myIsRunning = true;
    myIsNewDebuggerAppended = false;

    boolean accepted = false;
    boolean isFirstLine = true;

    while (!accepted) {
      final TheRScriptLine line = myScriptReader.getCurrentLine();

      if (isCommentOrSpaces(line.getText()) && isFirstLine) {
        forwardCommentsAndEmptyLines();

        return;
      }

      final TheRProcessResponse response = myProcess.execute(line.getText());

      handleResponse(response);

      accepted = response.getType() != TheRProcessResponseType.PLUS;
      isFirstLine = false;

      advanceScriptReader();
    }

    forwardCommentsAndEmptyLines();

    if (!myIsNewDebuggerAppended) {
      traceAndDebugFunctions(myProcess, myOutputReceiver);
    }
  }

  @NotNull
  @Override
  public String getResult() {
    if (hasNext()) {
      throw new IllegalStateException("GetResult should be called only if hasNext returns false");
    }

    return "";
  }

  private int getCurrentLineNumber() {
    return myScriptReader.getCurrentLine().getNumber();
  }

  private void forwardCommentsAndEmptyLines() throws TheRDebuggerException {
    while (isCommentOrSpaces(myScriptReader.getCurrentLine().getText())) {
      advanceScriptReader();
    }
  }

  private void handleResponse(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    appendError(response, myOutputReceiver);

    switch (response.getType()) {
      case DEBUGGING_IN:
        myIsNewDebuggerAppended = true;

        myDebuggerHandler.appendDebugger(
          myDebuggerFactory.getNotMainFunctionDebugger(
            myProcess,
            myDebuggerFactory,
            myDebuggerHandler,
            myOutputReceiver
          )
        );

        break;
      case RESPONSE:
        appendOutput(response, myOutputReceiver);

        break;
      case PLUS:
      case EMPTY:
        break;
      default:
        throw new UnexpectedResponseException(
          "Actual response type is not the same as expected: " +
          "[" +
          "actual: " + response.getType() + ", " +
          "expected: " +
          "[" + DEBUGGING_IN + ", " + RESPONSE + ", " + PLUS + ", " + EMPTY + "]" +
          "]"
        );
    }
  }

  private void advanceScriptReader() throws TheRDebuggerException {
    try {
      myScriptReader.advance();
    }
    catch (final IOException e) {
      throw new TheRDebuggerException(e);
    }
  }
}
