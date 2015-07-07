package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.*;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRDebuggerUtils;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

// TODO [dbg][test]
class TheRMainFunctionDebugger implements TheRFunctionDebugger {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheRFunctionResolver myFunctionResolver;

  @NotNull
  private final TheRLoadableVarHandler myVarHandler;

  @NotNull
  private final TheRScriptReader myScriptReader;

  @NotNull
  private List<TheRVar> myVars;

  private boolean myIsRunning;
  private boolean myIsNewDebuggerAppended;

  public TheRMainFunctionDebugger(@NotNull final TheRProcess process,
                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                  @NotNull final TheRFunctionResolver functionResolver,
                                  @NotNull final TheRLoadableVarHandler varHandler,
                                  @NotNull final TheRScriptReader scriptReader) {
    myProcess = process;
    myDebuggerHandler = debuggerHandler;
    myFunctionResolver = functionResolver;
    myVarHandler = varHandler;
    myScriptReader = scriptReader;

    myVars = Collections.emptyList();

    myIsRunning = false;
    myIsNewDebuggerAppended = false;
  }

  @NotNull
  @Override
  public TheRFunction getFunction() {
    return TheRDebugConstants.MAIN_FUNCTION;
  }

  public int getCurrentLineNumber() {
    return myScriptReader.getCurrentLine().getNumber();
  }

  @NotNull
  public List<TheRVar> getVars() {
    return myVars;
  }

  @Override
  public boolean hasNext() {
    return !myIsRunning || getCurrentLineNumber() != -1;
  }

  public void advance() throws IOException, InterruptedException {
    if (!hasNext()) {
      throw new IllegalStateException("Advance should be called only if hasNext returns true");
    }

    myIsRunning = true;
    myIsNewDebuggerAppended = false;

    boolean accepted = false;
    boolean isFirstLine = true;

    while (!accepted) {
      final TheRScriptLine line = myScriptReader.getCurrentLine();

      if (TheRDebuggerUtils.isCommentOrSpaces(line.getText()) && isFirstLine) {
        forwardCommentsAndEmptyLines();

        return;
      }

      final TheRProcessResponse response = myProcess.execute(line.getText());

      handleResponse(response);

      accepted = response.getType() != TheRProcessResponseType.PLUS;
      isFirstLine = false;

      myScriptReader.advance();
    }

    forwardCommentsAndEmptyLines();

    if (!myIsNewDebuggerAppended) {
      myVars = TheRDebuggerUtils.loadUnmodifiableVars(myProcess, myVarHandler);
    }
  }

  private void forwardCommentsAndEmptyLines() throws IOException {
    while (TheRDebuggerUtils.isCommentOrSpaces(myScriptReader.getCurrentLine().getText())) {
      myScriptReader.advance();
    }
  }

  private void handleResponse(@NotNull final TheRProcessResponse response) throws IOException, InterruptedException {
    switch (response.getType()) {
      case DEBUGGING_IN:
        myIsNewDebuggerAppended = true;

        myDebuggerHandler.appendDebugger(
          new TheRFunctionDebuggerImpl(
            myProcess,
            myDebuggerHandler,
            myFunctionResolver,
            myVarHandler,
            new TheRFunction(
              Collections.singletonList(
                TheRDebuggerUtils.loadFunctionName(myProcess)
              )
            )
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
        throw new IllegalStateException("Unexpected response from interpreter");
    }
  }
}
