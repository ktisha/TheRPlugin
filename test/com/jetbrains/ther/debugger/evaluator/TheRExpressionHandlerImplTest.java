package com.jetbrains.ther.debugger.evaluator;

import org.junit.Test;

import static com.jetbrains.ther.debugger.data.TheRCommands.*;
import static com.jetbrains.ther.debugger.data.TheRLanguageConstants.CLOSURE;
import static org.junit.Assert.assertEquals;

public class TheRExpressionHandlerImplTest {

  @Test
  public void identifierOnTheLast() {
    final TheRExpressionHandlerImpl handler = new TheRExpressionHandlerImpl();
    handler.setMaxFrameNumber(1);

    final String globalIdentifier = expressionOnFrameCommand(1, "abc");
    final String isFunction = typeOfCommand(globalIdentifier) + " == \"" + CLOSURE + "\"";
    final String isDebugged = isDebuggedCommand(globalIdentifier);

    assertEquals(
      "if (" + isFunction + " && " + isDebugged + ") " + attrCommand(globalIdentifier, "original") + " else " + globalIdentifier,
      handler.handle(1, "abc")
    );
  }

  @Test
  public void identifierOnThePrevious() {
    final TheRExpressionHandlerImpl handler = new TheRExpressionHandlerImpl();
    handler.setMaxFrameNumber(2);

    final String globalIdentifier = expressionOnFrameCommand(1, "abc");
    final String isFunction = typeOfCommand(globalIdentifier) + " == \"" + CLOSURE + "\"";
    final String isDebugged = isDebuggedCommand(globalIdentifier);

    assertEquals(
      "if (" + isFunction + " && " + isDebugged + ") " + attrCommand(globalIdentifier, "original") + " else " + globalIdentifier,
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
      expressionOnFrameCommand(1, "abc()"),
      handler.handle(1, "abc()")
    );
  }
}