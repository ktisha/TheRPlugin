package com.jetbrains.ther.debugger.frame;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import com.jetbrains.ther.debugger.mock.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;

import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TheRValueModifierImplTest {

  @Test
  public void illegal() {
    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      "text",
      TheRProcessResponseType.RESPONSE,
      TextRange.allOf("text"),
      ""
    );

    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(null, null);
    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(false);

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      process,
      factory,
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

    assertEquals(0, process.getCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(1, handler.myCounter);
  }

  @Test
  public void unexpectedResponseType() {
    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      "text",
      TheRProcessResponseType.RESPONSE,
      TextRange.allOf("text"),
      "error"
    );

    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(null, null);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      process,
      factory,
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

    assertEquals(1, process.getCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(Collections.singletonList("error"), receiver.getErrors());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(1, handler.myCounter);
  }

  @Test
  public void errorDuringExecution() {
    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      "",
      TheRProcessResponseType.EMPTY,
      TextRange.EMPTY_RANGE,
      "error"
    );

    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(null, null);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);
    final ErrorListener listener = new ErrorListener();

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      process,
      factory,
      receiver,
      handler,
      0
    );

    modifier.setValue("name", "value", listener);

    assertEquals(1, process.getCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(Collections.singletonList("error"), receiver.getErrors());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(1, handler.myCounter);
    assertEquals(1, listener.myCounter);
  }

  @Test
  public void exceptionDuringExecution() {
    final ExceptionDuringExecutionTheRProcess process = new ExceptionDuringExecutionTheRProcess();
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(null, null);
    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);
    final ExceptionListener listener = new ExceptionListener();

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      process,
      factory,
      new IllegalTheROutputReceiver(),
      handler,
      0
    );

    modifier.setValue("name", "value", listener);

    assertEquals(1, process.getCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(1, handler.myCounter);
    assertEquals(1, listener.myCounter);
  }

  @Test
  public void expression() {
    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      "",
      TheRProcessResponseType.EMPTY,
      TextRange.EMPTY_RANGE,
      ""
    );

    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(null, null);
    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);
    final SuccessListener listener = new SuccessListener();

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      process,
      factory,
      new IllegalTheROutputReceiver(),
      handler,
      0
    );

    modifier.setValue("name", "value", listener);

    assertEquals(1, process.getCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(1, handler.myCounter);
    assertEquals(1, listener.myCounter);
  }

  @Test
  public void inDebugExpression() {
    final InDebugTheRProcess process = new InDebugTheRProcess();

    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(null, null);
    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final SuccessListener listener = new SuccessListener();

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      process,
      factory,
      receiver,
      handler,
      0
    );

    modifier.setValue("name", "value", listener);

    assertEquals(2, process.getCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(Collections.singletonList("abc"), receiver.getErrors());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(1, handler.myCounter);
    assertEquals(1, listener.myCounter);
  }

  @Test
  public void function() {
    final String error = "error";

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      TheRDebugConstants.DEBUGGING_IN + ": def(c(1:5))",
      DEBUGGING_IN,
      TextRange.EMPTY_RANGE,
      error
    );

    final MockTheRFunctionDebugger debugger = new MockTheRFunctionDebugger("def", 2);
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(debugger, null);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final AlwaysSameResponseHandler handler = new AlwaysSameResponseHandler(true);
    final SuccessListener listener = new SuccessListener();

    final TheRValueModifierImpl modifier = new TheRValueModifierImpl(
      process,
      factory,
      receiver,
      handler,
      0
    );

    modifier.setValue("name", "value", listener);

    assertEquals(1, process.getCounter());
    assertEquals(2, debugger.getCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(1, factory.getNotMainCounter());
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

  private static class ExceptionDuringExecutionTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
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

  private static class InDebugTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() == 1) {
        return new TheRProcessResponse(
          TheRDebugConstants.DEBUG_AT + "2: x <- c(1:10)",
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "abc"
        );
      }

      if (getCounter() == 2) {
        return new TheRProcessResponse(
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