package com.jetbrains.ther.debugger.evaluator;

import com.jetbrains.ther.debugger.TheRDebuggerUtils;
import org.junit.Test;

import static com.jetbrains.ther.debugger.data.TheRCommands.expressionOnFrameCommand;
import static org.junit.Assert.assertEquals;

public class TheRExpressionHandlerImplTest {

  @Test
  public void identifierOnTheLast() {
    final TheRExpressionHandlerImpl handler = new TheRExpressionHandlerImpl();
    handler.setLastFrameNumber(1);

    assertEquals(
      TheRDebuggerUtils.calculateValueCommand(1, "abc"),
      handler.handle(1, "abc")
    );
  }

  @Test
  public void identifierOnThePrevious() {
    final TheRExpressionHandlerImpl handler = new TheRExpressionHandlerImpl();
    handler.setLastFrameNumber(2);

    assertEquals(
      TheRDebuggerUtils.calculateValueCommand(1, "abc"),
      handler.handle(1, "abc")
    );
  }

  @Test
  public void callOnTheLast() {
    final TheRExpressionHandlerImpl handler = new TheRExpressionHandlerImpl();
    handler.setLastFrameNumber(1);

    assertEquals(
      "abc()",
      handler.handle(1, "abc()")
    );
  }

  @Test
  public void callOnThePrevious() {
    final TheRExpressionHandlerImpl handler = new TheRExpressionHandlerImpl();
    handler.setLastFrameNumber(2);

    assertEquals(
      expressionOnFrameCommand(1, "abc()"),
      handler.handle(1, "abc()")
    );
  }
}