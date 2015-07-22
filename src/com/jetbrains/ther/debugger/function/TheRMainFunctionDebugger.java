package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheRScriptReader;
import com.jetbrains.ther.debugger.data.*;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.UnexpectedResponseException;
import com.jetbrains.ther.debugger.interpreter.TheRLoadableVarHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.isCommentOrSpaces;

// TODO [dbg][test]
class TheRMainFunctionDebugger implements TheRFunctionDebugger {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerFactory myDebuggerFactory;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheRLoadableVarHandler myVarHandler;

  @NotNull
  private final TheRScriptReader myScriptReader;

  @NotNull
  private List<TheRVar> myVars;

  private boolean myIsRunning;
  private boolean myIsNewDebuggerAppended;

  public TheRMainFunctionDebugger(@NotNull final TheRProcess process,
                                  @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                  @NotNull final TheRLoadableVarHandler varHandler,
                                  @NotNull final TheRScriptReader scriptReader) {
    myProcess = process;
    myDebuggerFactory = debuggerFactory;
    myDebuggerHandler = debuggerHandler;
    myVarHandler = varHandler;
    myScriptReader = scriptReader;

    myVars = Collections.emptyList();

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

  @NotNull
  public List<TheRVar> getVars() {
    return myVars;
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

      try {
        myScriptReader.advance();
      }
      catch (final IOException e) {
        throw new TheRDebuggerException(e);
      }
    }

    forwardCommentsAndEmptyLines();

    if (!myIsNewDebuggerAppended) {
      myVars = TheRProcessUtils.loadUnmodifiableVars(myProcess, myVarHandler);
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
    try {
      while (isCommentOrSpaces(myScriptReader.getCurrentLine().getText())) {
        myScriptReader.advance();
      }
    }
    catch (final IOException e) {
      throw new TheRDebuggerException(e);
    }
  }

  private void handleResponse(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    switch (response.getType()) {
      case DEBUGGING_IN:
        myIsNewDebuggerAppended = true;

        myDebuggerHandler.appendDebugger(
          myDebuggerFactory.getNotMainFunctionDebugger(
            myProcess,
            myDebuggerFactory,
            myDebuggerHandler,
            myVarHandler,
            getLocation()
          )
        );

        break;
      case RESPONSE:
        myDebuggerHandler.appendOutput(response.getText());

        break;
      case PLUS:
      case EMPTY:
        break;
      default:
        throw new UnexpectedResponseException("Unexpected response from interpreter");
    }
  }
}
