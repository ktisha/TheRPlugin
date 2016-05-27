package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.hash.HashMap;
import com.jetbrains.ther.debugger.MockitoUtils;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.mock.MockTheROutputReceiver;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.jetbrains.ther.debugger.data.TheRCommands.*;
import static com.jetbrains.ther.debugger.data.TheRFunctionConstants.*;
import static com.jetbrains.ther.debugger.data.TheRLanguageConstants.CLOSURE;
import static com.jetbrains.ther.debugger.data.TheRLanguageConstants.FUNCTION_TYPE;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.traceAndDebugFunctions;
import static com.jetbrains.ther.debugger.mock.MockTheRExecutor.LS_FUNCTIONS_ERROR;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TheRTraceAndDebugUtilsTest {

  @NotNull
  public static final String LS_FUNCTIONS_COMMAND = filterCommand(
    "function(x) x == \"" + CLOSURE + "\"",
    eapplyCommand(ENVIRONMENT_COMMAND, TYPEOF_FUNCTION)
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
    final String xFunctionName = "x";
    final String mainFunctionName = MAIN_FUNCTION_NAME;
    final String deviceFunctionName = SERVICE_FUNCTION_PREFIX + "device_init";
    final String xEnterFunctionName = SERVICE_FUNCTION_PREFIX + xFunctionName + SERVICE_ENTER_FUNCTION_SUFFIX;
    final String mainEnterFunctionName = SERVICE_FUNCTION_PREFIX + mainFunctionName + SERVICE_ENTER_FUNCTION_SUFFIX;

    final List<String> commands = getCommands(xFunctionName, mainFunctionName, xEnterFunctionName, mainEnterFunctionName);
    final List<TheRExecutionResult> results = getResults(xFunctionName, mainFunctionName, deviceFunctionName, xEnterFunctionName);
    final Map<String, List<TheRExecutionResult>> commandsAndResults = getCommandsAndResults(commands, results);

    final List<String> errors = Arrays.asList(
      LS_FUNCTIONS_ERROR,
      "error_" + xFunctionName + "_e",
      "error_" + xFunctionName + "_d",
      "error_" + mainFunctionName + "_e",
      "error_" + mainFunctionName + "_d"
    );

    final TheRExecutor executor = MockitoUtils.setupExecutor(commandsAndResults);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    traceAndDebugFunctions(executor, receiver);

    MockitoUtils.verifyExecutor(executor, commands);
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(errors, receiver.getErrors());

    verifyNoMoreInteractions(executor);
  }

  @NotNull
  private Map<String, List<TheRExecutionResult>> getCommandsAndResults(@NotNull final List<String> commands,
                                                                       @NotNull final List<TheRExecutionResult> results) {
    final Map<String, List<TheRExecutionResult>> commandsAndResults = new HashMap<String, List<TheRExecutionResult>>();

    for (int i = 0; i < commands.size(); i++) {
      commandsAndResults.put(commands.get(i), Collections.singletonList(results.get(i)));
    }

    return commandsAndResults;
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
}