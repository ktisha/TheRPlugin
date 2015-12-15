package com.jetbrains.ther.debugger.evaluator;

import org.junit.Test;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static org.junit.Assert.assertEquals;

public class TheRExpressionHandlerImplTest {

  @Test
  public void identifierOnTheLast() {
    final TheRExpressionHandlerImpl handler = new TheRExpressionHandlerImpl();
    handler.setMaxFrameNumber(1);

    final String globalIdentified = SYS_FRAME_COMMAND + "(1)$abc";
    final String isFunction = TYPEOF_COMMAND + "(" + globalIdentified + ") == \"" + CLOSURE + "\"";
    final String isDebugged = IS_DEBUGGED_COMMAND + "(" + globalIdentified + ")";

    assertEquals(
      "if (" + isFunction + " && " + isDebugged + ") " + ATTR_COMMAND + "(" + globalIdentified + ", \"original\") else " + globalIdentified,
      handler.handle(1, "abc")
    );
  }

  @Test
  public void identifierOnThePrevious() {
    final TheRExpressionHandlerImpl handler = new TheRExpressionHandlerImpl();
    handler.setMaxFrameNumber(2);

    final String globalIdentified = SYS_FRAME_COMMAND + "(1)$abc";
    final String isFunction = TYPEOF_COMMAND + "(" + globalIdentified + ") == \"" + CLOSURE + "\"";
    final String isDebugged = IS_DEBUGGED_COMMAND + "(" + globalIdentified + ")";

    assertEquals(
      "if (" + isFunction + " && " + isDebugged + ") " + ATTR_COMMAND + "(" + globalIdentified + ", \"original\") else " + globalIdentified,
      handler.handle(1, "abc")
    );
  }

  @Test
  public void callOnTheLast() {
    final TheRExpressionHandlerImpl handler = new TheRExpressionHandlerImpl();
    handler.setMaxFrameNumber(1);

    assertEquals(
      "abc()",
      handler.handle(1, "abc()")
    );
  }

  @Test
  public void callOnThePrevious() {
    final TheRExpressionHandlerImpl handler = new TheRExpressionHandlerImpl();
    handler.setMaxFrameNumber(2);

    assertEquals(
      SYS_FRAME_COMMAND + "(1)$abc()",
      handler.handle(1, "abc()")
    );
  }
}