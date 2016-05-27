package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

public class MockitoUtils {

  @NotNull
  public static TheRExecutor setupExecutor(@NotNull final Map<String, List<TheRExecutionResult>> commandsAndResults)
    throws TheRDebuggerException {
    final TheRExecutor result = Mockito.mock(TheRExecutor.class);

    for (Map.Entry<String, List<TheRExecutionResult>> commandAndResult : commandsAndResults.entrySet()) {
      final List<TheRExecutionResult> value = commandAndResult.getValue();

      if (value.size() == 1) {
        when(result.execute(commandAndResult.getKey())).thenReturn(value.get(0));
      }
      else {
        final TheRExecutionResult firstResult = value.get(0);
        final TheRExecutionResult[] otherResults = value.subList(1, value.size()).toArray(new TheRExecutionResult[value.size() - 1]);

        when(result.execute(commandAndResult.getKey())).thenReturn(firstResult, otherResults);
      }
    }

    return result;
  }

  public static void verifyExecutor(@NotNull final TheRExecutor executor, @NotNull final List<String> commands)
    throws TheRDebuggerException {
    final InOrder inOrder = inOrder(executor);

    for (final String command : commands) {
      inOrder.verify(executor).execute(command);
    }
  }
}
