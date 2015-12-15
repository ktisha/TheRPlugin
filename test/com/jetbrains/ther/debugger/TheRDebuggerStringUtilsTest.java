package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.mock.IllegalTheROutputReceiver;
import com.jetbrains.ther.debugger.mock.MockTheROutputReceiver;
import org.junit.Test;

import java.util.Collections;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.*;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.ENVIRONMENT;
import static org.junit.Assert.assertEquals;

public class TheRDebuggerStringUtilsTest {

  @Test
  public void emptyErrorAppending() {
    appendError(
      new TheRExecutionResult("", TheRExecutionResultType.EMPTY, TextRange.EMPTY_RANGE, ""),
      new IllegalTheROutputReceiver()
    );
  }

  @Test
  public void emptyResultAppending() {
    appendResult(
      new TheRExecutionResult("abc", TheRExecutionResultType.RESPONSE, TextRange.EMPTY_RANGE, ""),
      new IllegalTheROutputReceiver()
    );
  }

  @Test
  public void ordinaryErrorAppending() {
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    appendError(
      new TheRExecutionResult("", TheRExecutionResultType.EMPTY, TextRange.EMPTY_RANGE, "error"),
      receiver
    );

    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(Collections.singletonList("error"), receiver.getErrors());
  }

  @Test
  public void ordinaryResultAppending() {
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    appendResult(
      new TheRExecutionResult("output", TheRExecutionResultType.RESPONSE, new TextRange(0, 3), ""),
      receiver
    );

    assertEquals(Collections.singletonList("out"), receiver.getOutputs());
    assertEquals(Collections.emptyList(), receiver.getErrors());
  }

  @Test
  public void outerFunctionValueHandling() {
    assertEquals(
      "function(x) {\n" +
      "    x ^ 2\n" +
      "}",
      handleFunctionValue(
        "function(x) {\n" +
        "    x ^ 2\n" +
        "}"
      )
    );
  }

  @Test
  public void innerFunctionValueHandling() {
    assertEquals(
      "function(x) {\n" +
      "    x ^ 2\n" +
      "}",
      handleFunctionValue(
        "function(x) {\n" +
        "    x ^ 2\n" +
        "}\n" +
        "<" + ENVIRONMENT + ": 0xfffffff>"
      )
    );
  }

  @Test
  public void oneLineNextLineBegin() {
    final String line = "abc";

    assertEquals(line.length(), findNextLineBegin(line, 0));
  }

  @Test
  public void justLineBreaksNextLineBegin() {
    final String text = "\n\n\n\n\n";

    assertEquals(text.length(), findNextLineBegin(text, 0));
  }

  @Test
  public void lineBreakPointerNextLineBegin() {
    final String text = "abc\n\ndef";

    assertEquals(5, findNextLineBegin(text, 3));
  }

  @Test
  public void ordinaryNextLineBegin() {
    final String text = "abc\ndef";

    assertEquals(4, findNextLineBegin(text, 0));
  }

  @Test
  public void oneLineCurrentLineEnd() {
    final String line = "abc";

    assertEquals(line.length(), findCurrentLineEnd(line, 0));
  }

  @Test
  public void justLineBreaksCurrentLineEnd() {
    final String text = "\n\n\n\n";

    assertEquals(0, findCurrentLineEnd(text, 0));
  }

  @Test
  public void lineBreakPointerCurrentLineEnd() {
    final String text = "abc\n\ndef";

    assertEquals(3, findCurrentLineEnd(text, 3));
  }

  @Test
  public void ordinaryCurrentLineEnd() {
    final String text = "abc\ndef";

    assertEquals(3, findCurrentLineEnd(text, 0));
  }
}