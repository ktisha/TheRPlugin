package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.*;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRDebuggerUtils;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TheRMainFunctionDebugger implements TheRFunctionDebugger {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheRLoadableVarHandler myVarHandler;

  @NotNull
  private final TheRScriptReader myScriptReader;

  @NotNull
  private List<TheRVar> myVars;

  public TheRMainFunctionDebugger(@NotNull final TheRProcess process,
                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                  @NotNull final TheRLoadableVarHandler varHandler,
                                  @NotNull final TheRScriptReader scriptReader) throws IOException, InterruptedException {
    myProcess = process;
    myDebuggerHandler = debuggerHandler;
    myVarHandler = varHandler;
    myScriptReader = scriptReader;

    myVars = loadVars(); // TODO [dbg][update]
  }

  public int getCurrentLineNumber() {
    return myScriptReader.getCurrentLine().getNumber();
  }

  @NotNull
  public List<TheRVar> getVars() {
    return myVars;
  }

  public void advance() throws IOException, InterruptedException {
    if (getCurrentLineNumber() == -1) {
      throw new IllegalStateException(); // TODO [dbg][update]
    }

    boolean accepted = false;
    boolean isFirstLine = true;

    while (!accepted) {
      final TheRScriptLine line = myScriptReader.getCurrentLine();

      if (line.getText() == null) {
        myVars = Collections.emptyList(); // TODO [dbg][update]

        return;
      }

      if (TheRDebuggerUtils.isCommentOrSpaces(line.getText()) && isFirstLine) {
        myScriptReader.advance();

        return;
      }

      final TheRProcessResponse response = myProcess.execute(line.getText());

      handleResponse(response);

      accepted = response.getType() != TheRProcessResponseType.PLUS;
      isFirstLine = false;

      myScriptReader.advance();
    }

    myVars = loadVars();
  }

  private void handleResponse(@NotNull final TheRProcessResponse response) throws IOException, InterruptedException {
    switch (response.getType()) {
      case DEBUGGING_IN:
        myDebuggerHandler.appendDebugger(
          new TheRFunctionDebuggerImpl(
            myProcess,
            myDebuggerHandler,
            myVarHandler,
            new TheRFunction(
              Collections.singletonList(loadFunctionName())
            )
          )
        );

        break;
      case RESPONSE:
        myDebuggerHandler.appendNormalOutput(response.getText());

        break;
      case PLUS:
        break;
      default:
        throw new IllegalStateException(); // TODO [dbg][update]
    }
  }

  @NotNull
  private List<TheRVar> loadVars() throws IOException, InterruptedException {
    return Collections.unmodifiableList(
      TheRDebuggerUtils.loadVars(myProcess, myVarHandler)
    );
  }

  @NotNull
  private String loadFunctionName() throws IOException, InterruptedException {
    myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);
    myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);

    final String entryText = myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.RESPONSE);

    final int firstLineSeparator = entryText.indexOf(TheRDebugConstants.LINE_SEPARATOR);
    final int secondLineSeparator = entryText.indexOf(TheRDebugConstants.LINE_SEPARATOR, firstLineSeparator + 1);

    return entryText.substring(
      firstLineSeparator + "[1] \"".length() + "enter ".length() + 1,
      secondLineSeparator - "\"".length()
    );
  }
}
