package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRRuntimeException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.*;
import static com.jetbrains.ther.debugger.data.TheRCommands.EXECUTE_AND_STEP_COMMAND;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.FOR_LOOP_PREFIX;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.WHILE_LOOP_PREFIX;
import static com.jetbrains.ther.debugger.data.TheRResponseConstants.DEBUG_AT_LINE_PREFIX;
import static com.jetbrains.ther.debugger.data.TheRResponseConstants.EXITING_FROM_PREFIX;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.*;
import static com.jetbrains.ther.debugger.executor.TheRExecutorUtils.execute;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.traceAndDebugFunctions;

abstract class TheRFunctionDebuggerBase implements TheRFunctionDebugger {

  @NotNull
  private final TheRExecutor myExecutor;

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

  public TheRFunctionDebuggerBase(@NotNull final TheRExecutor executor,
                                  @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                  @NotNull final TheROutputReceiver outputReceiver,
                                  @NotNull final String functionName) throws TheRDebuggerException {
    myExecutor = executor;
    myDebuggerFactory = debuggerFactory;
    myDebuggerHandler = debuggerHandler;
    myOutputReceiver = outputReceiver;
    myFunctionName = functionName;

    myCurrentLineNumber = initCurrentLine();
    traceAndDebugFunctions(myExecutor, myOutputReceiver);

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

    final TheRExecutionResult result = myExecutor.execute(EXECUTE_AND_STEP_COMMAND);

    switch (result.getType()) {
      case CONTINUE_TRACE:
        handleContinueTrace(result);
        break;
      case DEBUG_AT:
        handleDebugAt(result);
        break;
      case DEBUGGING_IN:
        handleDebuggingIn(result);
        break;
      case EMPTY:
        handleEmpty(result);
        break;
      case EXITING_FROM:
        handleEndTrace(result);
        break;
      case RECURSIVE_EXITING_FROM:
        handleRecursiveEndTrace(result);
        break;
      default:
        throw new TheRUnexpectedExecutionResultException(
          "Actual type is not the same as expected: " +
          "[" +
          "actual: " + result.getType() + ", " +
          "expected: " +
          "[" +
          CONTINUE_TRACE + ", " +
          DEBUG_AT + ", " +
          DEBUGGING_IN + ", " +
          EMPTY + ", " +
          TheRExecutionResultType.EXITING_FROM + ", " +
          RECURSIVE_EXITING_FROM +
          "]" +
          "]"
        );
    }
  }

  @NotNull
  @Override
  public String getResult() {
    if (hasNext()) {
      throw new IllegalStateException("GetResult could be called only if hasNext returns false");
    }

    return myResult;
  }

  protected abstract void handleDebugAt(@NotNull final TheRExecutionResult result) throws TheRDebuggerException;

  @NotNull
  protected abstract TheRExecutionResultType getStartTraceType();

  protected int initCurrentLine() throws TheRDebuggerException {
    handleDebugAt(
      execute(
        myExecutor,
        EXECUTE_AND_STEP_COMMAND,
        DEBUG_AT
      ),
      false,
      true
    );

    return myCurrentLineNumber;
  }

  protected void handleDebugAt(@NotNull final TheRExecutionResult result,
                               final boolean enableTraceAndDebug,
                               final boolean extractLineNumber) throws TheRDebuggerException {
    appendResult(result, myOutputReceiver);
    appendError(result, myOutputReceiver);

    final String output = result.getOutput();
    final int debugAtIndex = findNextLineAfterResult(result);

    if (isBraceLoopEntrance(output, debugAtIndex)) {
      handleDebugAt(execute(myExecutor, EXECUTE_AND_STEP_COMMAND, DEBUG_AT), enableTraceAndDebug, true);
    }
    else {
      if (extractLineNumber) {
        myCurrentLineNumber = extractLineNumber(output, debugAtIndex);
      }

      if (enableTraceAndDebug) {
        traceAndDebugFunctions(myExecutor, myOutputReceiver);
      }
    }
  }

  protected void handleContinueTrace(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    handleEndTraceResult(result);
    appendError(result, myOutputReceiver);

    execute(myExecutor, EXECUTE_AND_STEP_COMMAND, DEBUG_AT, myOutputReceiver);
    execute(myExecutor, EXECUTE_AND_STEP_COMMAND, getStartTraceType(), myOutputReceiver);

    myCurrentLineNumber = initCurrentLine();
    traceAndDebugFunctions(myExecutor, myOutputReceiver);
  }

  protected void handleEndTrace(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    handleEndTraceResult(result);
    appendError(result, myOutputReceiver);

    final int lastExitingFromEntry = result.getOutput().lastIndexOf(EXITING_FROM_PREFIX);

    handleEndTraceReturn(result, lastExitingFromEntry);

    myCurrentLineNumber = -1;
  }

  protected void handleDebuggingIn(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    appendError(result, myOutputReceiver);

    myDebuggerHandler.appendDebugger(
      myDebuggerFactory.getFunctionDebugger(
        myExecutor,
        myDebuggerHandler,
        myOutputReceiver
      )
    );
  }

  protected void handleRecursiveEndTrace(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    handleEndTraceResult(result);
    appendError(result, myOutputReceiver);

    final RecursiveEndTraceData data = calculateRecursiveEndTraceData(result);

    handleEndTraceReturn(result, data.myLastExitingFrom);

    myCurrentLineNumber = -1;

    myDebuggerHandler.setDropFrames(data.myExitingFromCount);
  }

  protected void handleEmpty(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    appendError(result, myOutputReceiver);

    throw new TheRRuntimeException(result.getError());
  }

  protected void setCurrentLineNumber(final int currentLineNumber) {
    myCurrentLineNumber = currentLineNumber;
  }

  private int extractLineNumber(@NotNull final String output, final int debugAtIndex) {
    final int lineNumberBegin = debugAtIndex + DEBUG_AT_LINE_PREFIX.length();
    final int lineNumberEnd = output.indexOf(':', lineNumberBegin + 1);

    return Integer.parseInt(output.substring(lineNumberBegin, lineNumberEnd)) - 1; // -1 because of `MAIN_FUNCTION` declaration
  }

  private int findNextLineAfterResult(@NotNull final TheRExecutionResult result) {
    int index = result.getResultRange().getEndOffset();

    final String output = result.getOutput();

    while (index < output.length() && StringUtil.isLineBreak(output.charAt(index))) {
      index++;
    }

    return index;
  }

  private boolean isBraceLoopEntrance(@NotNull final String output, final int debugAtIndex) {
    final int lineNumberBegin = debugAtIndex + DEBUG_AT_LINE_PREFIX.length();
    final int loopEntranceBegin = output.indexOf(':', lineNumberBegin + 1) + 2;
    final int lines = StringUtil.countNewLines(output.substring(loopEntranceBegin));

    return lines > 1 && (
      output.startsWith(FOR_LOOP_PREFIX, loopEntranceBegin) ||
      output.startsWith(WHILE_LOOP_PREFIX, loopEntranceBegin)
    );
  }

  private void handleEndTraceResult(@NotNull final TheRExecutionResult result) {
    final TextRange resultRange = result.getResultRange();

    if (resultRange.getStartOffset() == 0) {
      appendResult(result, myOutputReceiver);
    }

    myResult = resultRange.substring(result.getOutput());
  }

  @NotNull
  private RecursiveEndTraceData calculateRecursiveEndTraceData(@NotNull final TheRExecutionResult result) {
    final String output = result.getOutput();

    int lastEntry = -1;
    int currentIndex = 0;
    int count = 0;

    while ((currentIndex = output.indexOf(EXITING_FROM_PREFIX, currentIndex)) != -1) {
      lastEntry = currentIndex;

      count++;
      currentIndex += EXITING_FROM_PREFIX.length();
    }

    return new RecursiveEndTraceData(lastEntry, count);
  }

  private void handleEndTraceReturn(@NotNull final TheRExecutionResult result, final int lastExitingFrom) throws TheRDebuggerException {
    final String output = result.getOutput();
    final int debugAtIndex = findDebugAtIndexInEndTraceReturn(result, lastExitingFrom);

    if (output.startsWith(DEBUG_AT_LINE_PREFIX, debugAtIndex)) {
      if (isBraceLoopEntrance(output, debugAtIndex)) {
        handleDebugAt(
          execute(myExecutor, EXECUTE_AND_STEP_COMMAND, DEBUG_AT),
          false,
          true
        );
      }

      myDebuggerHandler.setReturnLineNumber(extractLineNumber(output, debugAtIndex));
    }
  }

  private int findDebugAtIndexInEndTraceReturn(@NotNull final TheRExecutionResult result, final int lastExitingFrom) {
    if (result.getResultRange().getStartOffset() == 0) {
      return findNextLineBegin(
        result.getOutput(),
        lastExitingFrom + EXITING_FROM_PREFIX.length()
      );
    }
    else {
      return findNextLineAfterResult(result);
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
