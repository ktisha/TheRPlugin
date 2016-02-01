package com.jetbrains.ther.debugger.frame;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.mock.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.DEBUGGING_IN;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TheRValueModifierImplTest {

  @Test
  public void illegal() {
    final AlwaysSameResultTheRExecutor executor = new AlwaysSameResultTheRExecutor(
      "text",
      TheRExecutionResultType.RESPONSE,
      TextRange.allOf("text"),
      ""
    );

    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(false);

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      new IllegalTheROutputReceiver(),
      handler,
      0
    );

    try {
      modifier.setValue("name", "value", new IllegalListener());

      fail();
    }
    catch (final IllegalStateException ignored) {
    }

    assertEquals(0, executor.getCounter());
    assertEquals(1, handler.myCounter);
  }

  @Test
  public void unexpectedResultType() {
    final AlwaysSameResultTheRExecutor executor = new AlwaysSameResultTheRExecutor(
      "text",
      TheRExecutionResultType.RESPONSE,
      TextRange.allOf("text"),
      "error"
    );

    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      receiver,
      handler,
      0
    );

    try {
      modifier.setValue("name", "value", new IllegalListener());

      fail();
    }
    catch (final IllegalStateException ignored) {
    }

    assertEquals(1, executor.getCounter());
    assertEquals(Collections.singletonList("error"), receiver.getErrors());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(1, handler.myCounter);
  }

  @Test
  public void errorDuringExecution() {
    final AlwaysSameResultTheRExecutor executor = new AlwaysSameResultTheRExecutor(
      "",
      TheRExecutionResultType.EMPTY,
      TextRange.EMPTY_RANGE,
      "error"
    );

    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);
    final ErrorListener listener = new ErrorListener();

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      receiver,
      handler,
      0
    );

    modifier.setValue("name", "value", listener);

    assertEquals(1, executor.getCounter());
    assertEquals(Collections.singletonList("error"), receiver.getErrors());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(1, handler.myCounter);
    assertEquals(1, listener.myCounter);
  }

  @Test
  public void exceptionDuringExecution() {
    final ExceptionDuringExecutionTheRExecutor executor = new ExceptionDuringExecutionTheRExecutor();
    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);
    final ExceptionListener listener = new ExceptionListener();

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      new IllegalTheROutputReceiver(),
      handler,
      0
    );

    modifier.setValue("name", "value", listener);

    assertEquals(1, executor.getCounter());
    assertEquals(1, handler.myCounter);
    assertEquals(1, listener.myCounter);
  }

  @Test
  public void expression() {
    final AlwaysSameResultTheRExecutor executor = new AlwaysSameResultTheRExecutor(
      "",
      TheRExecutionResultType.EMPTY,
      TextRange.EMPTY_RANGE,
      ""
    );

    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);
    final SuccessListener listener = new SuccessListener();

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      new IllegalTheROutputReceiver(),
      handler,
      0
    );

    modifier.setValue("name", "value", listener);

    assertEquals(1, executor.getCounter());
    assertEquals(1, handler.myCounter);
    assertEquals(1, listener.myCounter);
  }

  @Test
  public void inDebugExpression() {
    final InDebugTheRExecutor executor = new InDebugTheRExecutor();

    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final SuccessListener listener = new SuccessListener();

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      receiver,
      handler,
      0
    );

    modifier.setValue("name", "value", listener);

    assertEquals(2, executor.getCounter());
    assertEquals(Collections.singletonList("abc"), receiver.getErrors());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(1, handler.myCounter);
    assertEquals(1, listener.myCounter);
  }

  @Test
  public void function() {
    final String error = "error";

    final AlwaysSameResultTheRExecutor executor = new AlwaysSameResultTheRExecutor(
      TheRDebugConstants.DEBUGGING_IN + ": def(c(1:5))\n" +
      DEBUG + ": {\n" +
      "    .doTrace(" + SERVICE_FUNCTION_PREFIX + "def" + SERVICE_ENTER_FUNCTION_SUFFIX + "(), \"on entry\")\n" +
      "    {\n" +
      "        print(\"x\")\n" +
      "    }\n" +
      "}",
      DEBUGGING_IN,
      TextRange.EMPTY_RANGE,
      error
    );

    final MockTheRFunctionDebugger debugger = new MockTheRFunctionDebugger("def", 2);
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(debugger);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);
    final SuccessListener listener = new SuccessListener();

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      executor,
      factory,
      receiver,
      handler,
      0
    );

    modifier.setValue("name", "value", listener);

    assertEquals(1, executor.getCounter());
    assertEquals(2, debugger.getCounter());
    assertEquals(1, factory.getCounter());
    assertEquals(Collections.singletonList(error), receiver.getErrors());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(1, handler.myCounter);
    assertEquals(1, listener.myCounter);
  }

  private static class AlwaysSameResponseHandler extends IllegalTheRValueModifierHandler {

    private final boolean myResponse;

    private int myCounter = 0;

    public AlwaysSameResponseHandler(final boolean response) {
      myResponse = response;
    }

    @Override
    public boolean isModificationAvailable(final int frameNumber) {
      myCounter++;

      return myResponse;
    }
  }

  private static class IllegalListener implements TheRValueModifier.Listener {

    @Override
    public void onSuccess() {
      throw new IllegalStateException("OnSuccess shouldn't be called");
    }

    @Override
    public void onError(@NotNull final String error) {
      throw new IllegalStateException("OnError shouldn't be called");
    }

    @Override
    public void onError(@NotNull final Exception e) {
      throw new IllegalStateException("OnError shouldn't be called");
    }
  }

  private static class ErrorListener extends IllegalListener {

    private int myCounter = 0;

    @Override
    public void onError(@NotNull final String error) {
      myCounter++;
    }
  }

  private static class ExceptionDuringExecutionTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      throw new TheRDebuggerException("");
    }
  }

  private static class ExceptionListener extends IllegalListener {

    private int myCounter = 0;

    @Override
    public void onError(@NotNull final Exception e) {
      myCounter++;
    }
  }

  private static class SuccessListener extends IllegalListener {

    private int myCounter = 0;

    @Override
    public void onSuccess() {
      myCounter++;
    }
  }

  private static class InDebugTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() == 1) {
        return new TheRExecutionResult(
          TheRDebugConstants.DEBUG_AT_LINE_PREFIX + "2: x <- c(1:10)",
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "abc"
        );
      }

      if (getCounter() == 2) {
        return new TheRExecutionResult(
          "",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }
}