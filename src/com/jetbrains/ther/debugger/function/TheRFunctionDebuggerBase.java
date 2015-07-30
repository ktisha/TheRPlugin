package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.TheRDebuggerStringUtils;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.appendError;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.appendResult;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.traceAndDebugFunctions;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.RESPONSE;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.START_TRACE_BRACE;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.execute;

// TODO [dbg][test]
abstract class TheRFunctionDebuggerBase implements TheRFunctionDebugger {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerFactory myDebuggerFactory;

  @NotNull
  private final TheRFunctionDebuggerHandler myDebuggerHandler;

  @NotNull
  private final TheROutputReceiver myOutputReceiver;

  @NotNull
  private final String myFunctionName;

  private int myCurrentLineNumber;

  @NotNull
  private String myResult;

  public TheRFunctionDebuggerBase(@NotNull final TheRProcess process,
                                  @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                  @NotNull final TheROutputReceiver outputReceiver,
                                  @NotNull final String functionName) throws TheRDebuggerException {
    myProcess = process;
    myDebuggerFactory = debuggerFactory;
    myDebuggerHandler = debuggerHandler;
    myOutputReceiver = outputReceiver;
    myFunctionName = functionName;

    myCurrentLineNumber = initCurrentLine();
    traceAndDebugFunctions(myProcess, myOutputReceiver);

    myResult = "";
  }

  @NotNull
  @Override
  public TheRLocation getLocation() {
    return new TheRLocation(myFunctionName, myCurrentLineNumber);
  }

  @Override
  public boolean hasNext() {
    return myCurrentLineNumber != -1;
  }

  @Override
  public void advance() throws TheRDebuggerException {
    if (!hasNext()) {
      throw new IllegalStateException("Advance could be called only if hasNext returns true");
    }

    handleResponse(myProcess.execute(EXECUTE_AND_STEP_COMMAND));
  }

  @NotNull
  @Override
  public String getResult() {
    if (hasNext()) {
      throw new IllegalStateException("GetResult could be called only if hasNext returns false");
    }

    return myResult;
  }

  protected abstract int initCurrentLine() throws TheRDebuggerException;

  protected abstract void handleResponse(@NotNull final TheRProcessResponse response) throws TheRDebuggerException;

  protected int loadLineNumber() throws TheRDebuggerException {
    final TheRProcessResponse response = execute(
      myProcess,
      EXECUTE_AND_STEP_COMMAND,
      TheRProcessResponseType.DEBUG_AT
    );

    appendError(response, myOutputReceiver);

    return extractLineNumber(response.getOutput(), 0);
  }

  protected void handleDebugAt(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    appendResult(response, myOutputReceiver);
    appendError(response, myOutputReceiver);

    myCurrentLineNumber = extractLineNumber(
      response.getOutput(),
      findNextLineAfterResultBegin(response)
    );

    traceAndDebugFunctions(myProcess, myOutputReceiver);
  }

  protected void handleContinueTrace(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    appendResult(response, myOutputReceiver);
    appendError(response, myOutputReceiver);

    execute(myProcess, EXECUTE_AND_STEP_COMMAND, RESPONSE, myOutputReceiver);
    execute(myProcess, EXECUTE_AND_STEP_COMMAND, RESPONSE, myOutputReceiver);
    execute(myProcess, EXECUTE_AND_STEP_COMMAND, RESPONSE, myOutputReceiver);
    execute(myProcess, EXECUTE_AND_STEP_COMMAND, START_TRACE_BRACE, myOutputReceiver);

    myCurrentLineNumber = loadLineNumber();
    traceAndDebugFunctions(myProcess, myOutputReceiver);
  }

  protected void handleEndTrace(@NotNull final TheRProcessResponse response) {
    handleEndTraceResult(response);
    appendError(response, myOutputReceiver);

    final int lastExitingFromEntry = response.getOutput().lastIndexOf(EXITING_FROM);

    handleEndTraceReturnLineNumber(response, lastExitingFromEntry);

    myCurrentLineNumber = -1;
  }

  protected void handleDebuggingIn(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    appendError(response, myOutputReceiver);

    myDebuggerHandler.appendDebugger(
      myDebuggerFactory.getNotMainFunctionDebugger(
        myProcess,
        myDebuggerHandler,
        myOutputReceiver
      )
    );
  }

  protected void handleRecursiveEndTrace(@NotNull final TheRProcessResponse response) {
    handleEndTraceResult(response);
    appendError(response, myOutputReceiver);

    final RecursiveEndTraceData data = calculateRecursiveEndTraceData(response);

    handleEndTraceReturnLineNumber(response, data.myLastExitingFrom);

    myCurrentLineNumber = -1;

    myDebuggerHandler.setDropFrames(data.myExitingFromCount);
  }

  private int extractLineNumber(@NotNull final String text, final int index) {
    final int lineNumberBegin = index + DEBUG_AT.length();
    final int lineNumberEnd = text.indexOf(':', lineNumberBegin + 1);

    return Integer.parseInt(text.substring(lineNumberBegin, lineNumberEnd)) - 1;
  }

  private int findNextLineAfterResultBegin(@NotNull final TheRProcessResponse response) {
    int result = response.getResultRange().getEndOffset();

    final String output = response.getOutput();

    while (result < output.length() && StringUtil.isLineBreak(output.charAt(result))) {
      result++;
    }

    return result;
  }

  private void handleEndTraceResult(@NotNull final TheRProcessResponse response) {
    final TextRange resultRange = response.getResultRange();

    if (resultRange.getStartOffset() == 0) {
      appendResult(response, myOutputReceiver);
    }

    myResult = resultRange.substring(response.getOutput());
  }

  @NotNull
  private RecursiveEndTraceData calculateRecursiveEndTraceData(@NotNull final TheRProcessResponse response) {
    final String output = response.getOutput();

    int lastEntry = -1;
    int currentIndex = 0;
    int count = 0;

    while ((currentIndex = output.indexOf(EXITING_FROM, currentIndex)) != -1) {
      lastEntry = currentIndex;

      count++;
      currentIndex += EXITING_FROM.length();
    }

    return new RecursiveEndTraceData(lastEntry, count);
  }

  private void handleEndTraceReturnLineNumber(@NotNull final TheRProcessResponse response, final int lastExitingFrom) {
    final int returnLineNumber = extractLineNumberIfPossible(
      response,
      findDebugAtIndexInEndTrace(response, lastExitingFrom)
    );

    if (returnLineNumber != -1) {
      myDebuggerHandler.setReturnLineNumber(returnLineNumber);
    }
  }

  private int extractLineNumberIfPossible(@NotNull final TheRProcessResponse response, final int debugAtIndex) {
    final String output = response.getOutput();

    if (debugAtIndex == output.length() || !output.startsWith(DEBUG_AT, debugAtIndex)) {
      return -1;
    }

    return extractLineNumber(output, debugAtIndex);
  }

  private int findDebugAtIndexInEndTrace(@NotNull final TheRProcessResponse response, final int lastExitingFrom) {
    if (response.getResultRange().getStartOffset() == 0) {
      return TheRDebuggerStringUtils.findNextLineBegin(
        response.getOutput(),
        lastExitingFrom + EXITING_FROM.length()
      );
    }
    else {
      return findNextLineAfterResultBegin(response);
    }
  }

  private static class RecursiveEndTraceData {

    private final int myLastExitingFrom;
    private final int myExitingFromCount;

    private RecursiveEndTraceData(final int lastExitingFrom, final int exitingFromCount) {
      myLastExitingFrom = lastExitingFrom;
      myExitingFromCount = exitingFromCount;
    }
  }
}
