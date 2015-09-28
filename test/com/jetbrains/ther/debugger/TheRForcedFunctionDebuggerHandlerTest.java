package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.mock.MockTheRExecutor;
import com.jetbrains.ther.debugger.mock.MockTheRFunctionDebugger;
import com.jetbrains.ther.debugger.mock.MockTheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.mock.MockTheROutputReceiver;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class TheRForcedFunctionDebuggerHandlerTest {

  @Test
  public void stack1() throws TheRDebuggerException {
    /*
    def() {
      instruction1
    }
    */

    final String result = "[1] 1 2 3";

    final Stack1TheRFunctionDebugger debugger = new Stack1TheRFunctionDebugger();
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(debugger);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRForcedFunctionDebuggerHandler handler = new TheRForcedFunctionDebuggerHandler(
      new IllegalTheRExecutor(),
      factory,
      receiver
    );

    //noinspection StatementWithEmptyBody
    while (handler.advance()) {
    }

    assertEquals(1, debugger.getCounter());
    assertEquals(1, factory.getCounter());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(result, handler.getResult());
  }

  @Test
  public void stack21() throws TheRDebuggerException {
    /*
    def() {
      instruction1
      abc() {
        instruction1
        instruction2
      }
      instruction2
    }
    */

    final String result = "[1] 1 2 3";

    final MockTheRFunctionDebugger secondFunctionDebugger = new MockTheRFunctionDebugger("abc", 2);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack211TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(firstFunctionDebugger);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRForcedFunctionDebuggerHandler handler = new TheRForcedFunctionDebuggerHandler(
      new IllegalTheRExecutor(),
      factory,
      receiver
    );

    //noinspection StatementWithEmptyBody
    while (handler.advance()) {
    }

    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(1, factory.getCounter());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(result, handler.getResult());
  }

  @Test
  public void stack22() throws TheRDebuggerException {
    /*
    def() {
      instruction1
      abc() {
        instruction1
        instruction2
      }
    }
    */

    final String result = "[1] 1 2 3";

    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack222TheRFunctionDebugger();
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack221TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(firstFunctionDebugger);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRForcedFunctionDebuggerHandler handler = new TheRForcedFunctionDebuggerHandler(
      new IllegalTheRExecutor(),
      factory,
      receiver
    );

    //noinspection StatementWithEmptyBody
    while (handler.advance()) {
    }

    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, factory.getCounter());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(result, handler.getResult());
  }

  @Test
  public void stack3() throws TheRDebuggerException {
    /*
    def() {
      instruction1
      abc() {
        instruction1
        ghi() {
          instruction1
          instruction2
        }
      }
      instruction2
    }
    */

    final String result = "[1] 1 2 3";

    final MockTheRFunctionDebugger thirdFunctionDebugger = new Stack33TheRFunctionDebugger();
    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack32TheRFunctionDebugger(thirdFunctionDebugger);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack31TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(firstFunctionDebugger);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRForcedFunctionDebuggerHandler handler = new TheRForcedFunctionDebuggerHandler(
      new IllegalTheRExecutor(),
      factory,
      receiver
    );

    //noinspection StatementWithEmptyBody
    while (handler.advance()) {
    }

    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(1, factory.getCounter());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(result, handler.getResult());
  }

  private static class IllegalTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      throw new IllegalStateException("DoExecute shouldn't be called");
    }
  }

  private static class Stack1TheRFunctionDebugger extends MockTheRFunctionDebugger {

    public Stack1TheRFunctionDebugger() {
      super("", 1);
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      throw new IllegalStateException("GetLocation shouldn't be called");
    }

    @NotNull
    @Override
    public String getResult() {
      return "[1] 1 2 3";
    }
  }

  private static class Stack211TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack211TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger debugger) {
      super("def", 3);

      myNextFunctionDebugger = debugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      throw new IllegalStateException("GetLocation shouldn't be called");
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        getHandler().appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      return "[1] 1 2 3";
    }
  }

  private static class Stack221TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack221TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger debugger) {
      super("def", 2);

      myNextFunctionDebugger = debugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      throw new IllegalStateException("GetLocation shouldn't be called");
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        myNextFunctionDebugger.setHandler(getHandler());
        getHandler().appendDebugger(myNextFunctionDebugger);
      }
    }
  }

  private static class Stack222TheRFunctionDebugger extends MockTheRFunctionDebugger {

    public Stack222TheRFunctionDebugger() {
      super("abc", 2);
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      throw new IllegalStateException("GetLocation shouldn't be called");
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        getHandler().setDropFrames(2);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      return "[1] 1 2 3";
    }
  }

  private static class Stack31TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack31TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger nextFunctionDebugger) {
      super("def", 3);

      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        myNextFunctionDebugger.setHandler(getHandler());
        getHandler().appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      return "[1] 1 2 3";
    }
  }

  private static class Stack32TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack32TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger nextFunctionDebugger) {
      super("abc", 2);

      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        myNextFunctionDebugger.setHandler(getHandler());
        getHandler().appendDebugger(myNextFunctionDebugger);
      }
    }
  }

  private static class Stack33TheRFunctionDebugger extends MockTheRFunctionDebugger {

    public Stack33TheRFunctionDebugger() {
      super("ghi", 2);
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        getHandler().setDropFrames(2);
      }
    }
  }
}