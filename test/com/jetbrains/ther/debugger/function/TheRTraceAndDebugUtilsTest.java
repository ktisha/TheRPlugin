package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.data.TheRResponseConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRCommands.*;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.data.TheRLanguageConstants.CLOSURE;
import static com.jetbrains.ther.debugger.data.TheRLanguageConstants.FUNCTION_TYPE;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.traceAndDebugFunctions;
import static com.jetbrains.ther.debugger.mock.MockTheRExecutor.LS_FUNCTIONS_ERROR;
import static org.mockito.Mockito.*;

public class TheRTraceAndDebugUtilsTest {

  @NotNull
  public static final String LS_FUNCTIONS_COMMAND = filterCommand(
    "function(x) x == \"" + CLOSURE,
    eapplyCommand(
      TheRResponseConstants.ENVIRONMENT + "()",
      TYPEOF_FUNCTION)
  );

  @NotNull
  public static final TheRExecutionResult NO_FUNCTIONS_RESULT = new TheRExecutionResult(
    "named list()",
    TheRExecutionResultType.RESPONSE,
    TextRange.allOf("named list()"),
    LS_FUNCTIONS_ERROR
  );

  @Test
  public void empty() throws TheRDebuggerException {
    final TheRExecutor executor = mock(TheRExecutor.class);
    final TheROutputReceiver receiver = mock(TheROutputReceiver.class);

    when(executor.execute(LS_FUNCTIONS_COMMAND)).thenReturn(NO_FUNCTIONS_RESULT);

    traceAndDebugFunctions(executor, receiver);

    verify(executor, times(1)).execute(LS_FUNCTIONS_COMMAND);
    verify(receiver, times(1)).receiveError(LS_FUNCTIONS_ERROR);

    verifyNoMoreInteractions(executor);
    verifyNoMoreInteractions(receiver);
  }

  @Test
  public void ordinary() throws TheRDebuggerException {
    final TheRExecutor executor = mock(TheRExecutor.class);
    final TheROutputReceiver receiver = mock(TheROutputReceiver.class);

    final String xFunctionName = "x";
    final String mainFunctionName = MAIN_FUNCTION_NAME;
    final String deviceFunctionName = SERVICE_FUNCTION_PREFIX + "device_init";
    final String xEnterFunctionName = SERVICE_FUNCTION_PREFIX + xFunctionName + SERVICE_ENTER_FUNCTION_SUFFIX;
    final String mainEnterFunctionName = SERVICE_FUNCTION_PREFIX + mainFunctionName + SERVICE_ENTER_FUNCTION_SUFFIX;

    final List<String> commands = getCommands(xFunctionName, mainFunctionName, xEnterFunctionName, mainEnterFunctionName);
    final List<TheRExecutionResult> results = getResults(xFunctionName, mainFunctionName, deviceFunctionName, xEnterFunctionName);
    final List<String> errors = Arrays.asList(
      LS_FUNCTIONS_ERROR,
      "error_" + xFunctionName + "_e",
      "error_" + xFunctionName + "_d",
      "error_" + mainFunctionName + "_e",
      "error_" + mainFunctionName + "_d"
    );

    setupExecutor(executor, commands, results);

    traceAndDebugFunctions(executor, receiver);

    verifyExecutor(executor, commands);
    verifyReceiver(receiver, errors);

    verifyNoMoreInteractions(executor);
    verifyNoMoreInteractions(receiver);
  }

  @NotNull
  private List<String> getCommands(@NotNull final String xFunctionName,
                                   @NotNull final String mainFunctionName,
                                   @NotNull final String xEnterFunctionName,
                                   @NotNull final String mainEnterFunctionName) {
    return Arrays.asList(
      LS_FUNCTIONS_COMMAND,

      xEnterFunctionName + " <- function() { print(\"" + xFunctionName + "\") }",
      traceCommand(xFunctionName, xEnterFunctionName),
      debugCommand(xFunctionName),

      mainEnterFunctionName + " <- function() { print(\"" + mainFunctionName + "\") }",
      traceCommand(mainFunctionName, mainEnterFunctionName),
      debugCommand(mainFunctionName)
    );
  }

  @NotNull
  private List<TheRExecutionResult> getResults(@NotNull final String xFunctionName,
                                               @NotNull final String mainFunctionName,
                                               @NotNull final String deviceFunctionName,
                                               @NotNull final String xEnterFunctionName) {
    final String lsFunctionsOutput = "$" + xFunctionName + "\n" +
                                     FUNCTION_TYPE + "\n\n" +
                                     "$" + xEnterFunctionName + "\n" +
                                     FUNCTION_TYPE + "\n\n" +
                                     "$" + deviceFunctionName + "\n" +
                                     FUNCTION_TYPE + "\n\n" +
                                     "$" + mainFunctionName + "\n" +
                                     FUNCTION_TYPE;

    return Arrays.asList(
      new TheRExecutionResult(
        lsFunctionsOutput,
        TheRExecutionResultType.RESPONSE,
        TextRange.allOf(lsFunctionsOutput),
        LS_FUNCTIONS_ERROR
      ),
      new TheRExecutionResult(
        "",
        TheRExecutionResultType.EMPTY,
        TextRange.EMPTY_RANGE,
        "error_" + xFunctionName + "_e"
      ),
      new TheRExecutionResult(
        "[1] \"" + xFunctionName + "\"",
        TheRExecutionResultType.RESPONSE,
        TextRange.allOf("[1] \"" + xFunctionName + "\""),
        "error_" + xFunctionName + "_t"
      ),
      new TheRExecutionResult(
        "",
        TheRExecutionResultType.EMPTY,
        TextRange.EMPTY_RANGE,
        "error_" + xFunctionName + "_d"
      ),
      new TheRExecutionResult(
        "",
        TheRExecutionResultType.EMPTY,
        TextRange.EMPTY_RANGE,
        "error_" + mainFunctionName + "_e"
      ),
      new TheRExecutionResult(
        "[1] \"" + mainFunctionName + "\"",
        TheRExecutionResultType.RESPONSE,
        TextRange.allOf("[1] \"" + mainFunctionName + "\""),
        "error_" + mainFunctionName + "_t"
      ),
      new TheRExecutionResult(
        "",
        TheRExecutionResultType.EMPTY,
        TextRange.EMPTY_RANGE,
        "error_" + mainFunctionName + "_d"
      )
    );
  }


  private void setupExecutor(@NotNull final TheRExecutor executor,
                             @NotNull final List<String> commands,
                             @NotNull final List<TheRExecutionResult> results) throws TheRDebuggerException {
    final Iterator<String> commandsIterator = commands.iterator();
    final Iterator<TheRExecutionResult> resultsIterator = results.iterator();

    while (commandsIterator.hasNext()) {
      when(executor.execute(commandsIterator.next())).thenReturn(resultsIterator.next());
    }
  }

  private void verifyExecutor(@NotNull final TheRExecutor executor, @NotNull final List<String> commands) throws TheRDebuggerException {
    final InOrder inOrder = inOrder(executor);

    for (final String command : commands) {
      inOrder.verify(executor).execute(command);
    }
  }

  private void verifyReceiver(@NotNull final TheROutputReceiver receiver, @NotNull final List<String> errors) {
    final InOrder inOrder = inOrder(receiver);

    for (final String error : errors) {
      inOrder.verify(receiver).receiveError(error);
    }
  }
}