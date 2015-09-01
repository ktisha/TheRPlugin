package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.TheRScriptReader;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRScriptLine;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.executor.TheRExecutorUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.appendOutput;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.isCommentOrSpaces;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.MAIN_FUNCTION_NAME;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.*;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.traceAndDebugFunctions;

class TheRMainFunctionDebugger implements TheRFunctionDebugger {

  @NotNull
  private final TheRExecutor myExecutor;

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

  public TheRMainFunctionDebugger(@NotNull final TheRExecutor executor,
                                  @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                  @NotNull final TheROutputReceiver outputReceiver,
                                  @NotNull final TheRScriptReader scriptReader) {
    myExecutor = executor;
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
      MAIN_FUNCTION_NAME,
      getCurrentLineNumber()
    );
  }

  @Override
  public boolean hasNext() {
    return !myIsRunning || getCurrentLineNumber() != -1;
  }

  public void advance() throws TheRDebuggerException {
    if (!hasNext()) {
      throw new IllegalStateException("Advance could be called only if hasNext returns true");
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

      final TheRExecutionResult result = TheRExecutorUtils.execute(myExecutor, line.getText(), myOutputReceiver);

      handleExecutionResult(result);

      accepted = result.getType() != TheRExecutionResultType.PLUS;
      isFirstLine = false;

      advanceScriptReader();
    }

    forwardCommentsAndEmptyLines();

    if (!myIsNewDebuggerAppended) {
      traceAndDebugFunctions(myExecutor, myOutputReceiver);
    }
  }

  @NotNull
  @Override
  public String getResult() {
    if (hasNext()) {
      throw new IllegalStateException("GetResult could be called only if hasNext returns false");
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

  private void handleExecutionResult(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    switch (result.getType()) {
      case DEBUGGING_IN:
        myIsNewDebuggerAppended = true;

        myDebuggerHandler.appendDebugger(
          myDebuggerFactory.getNotMainFunctionDebugger(
            myExecutor,
            myDebuggerHandler,
            myOutputReceiver
          )
        );

        break;
      case RESPONSE:
        appendOutput(result, myOutputReceiver);

        break;
      case PLUS:
      case EMPTY:
        break;
      default:
        throw new TheRUnexpectedExecutionResultException(
          "Actual type is not the same as expected: " +
          "[" +
          "actual: " + result.getType() + ", " +
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
