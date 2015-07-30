package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import com.jetbrains.ther.debugger.mock.IllegalTheROutputReceiver;
import com.jetbrains.ther.debugger.mock.TheROutputErrorReceiver;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.*;
import static org.junit.Assert.*;

public class TheRDebuggerStringUtilsTest {

  @Test
  public void emptyErrorAppending() {
    appendError(
      new TheRProcessResponse("", TheRProcessResponseType.EMPTY, TextRange.EMPTY_RANGE, ""),
      new IllegalTheROutputReceiver()
    );
  }

  @Test
  public void emptyOutputAppending() {
    appendOutput(
      new TheRProcessResponse("", TheRProcessResponseType.EMPTY, TextRange.EMPTY_RANGE, ""),
      new IllegalTheROutputReceiver()
    );
  }

  @Test
  public void emptyResultAppending() {
    appendResult(
      new TheRProcessResponse("abc", TheRProcessResponseType.RESPONSE, TextRange.EMPTY_RANGE, ""),
      new IllegalTheROutputReceiver()
    );
  }

  @Test
  public void ordinaryErrorAppending() {
    final TheROutputErrorReceiver receiver = new TheROutputErrorReceiver("error");

    appendError(
      new TheRProcessResponse("", TheRProcessResponseType.EMPTY, TextRange.EMPTY_RANGE, "error"),
      receiver
    );

    assertEquals(1, receiver.getErrorReceived());
  }

  @Test
  public void ordinaryOutputAppending() {
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver("output");

    appendOutput(
      new TheRProcessResponse("output", TheRProcessResponseType.RESPONSE, TextRange.allOf("output"), ""),
      receiver
    );

    assertEquals(1, receiver.myOutputReceived);
  }

  @Test
  public void ordinaryResultAppending() {
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver("out");

    appendResult(
      new TheRProcessResponse("output", TheRProcessResponseType.RESPONSE, new TextRange(0, 3), ""),
      receiver
    );

    assertEquals(1, receiver.myOutputReceived);
  }

  @Test
  public void commentChecking() {
    assertTrue(isCommentOrSpaces(" # abc "));
  }

  @Test
  public void spacesChecking() {
    assertTrue(isCommentOrSpaces("  "));
  }

  @Test
  public void nullChecking() {
    assertFalse(isCommentOrSpaces(null));
  }

  @Test
  public void ordinaryChecking() {
    assertFalse(isCommentOrSpaces(" abc "));
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

  private static class MockTheROutputReceiver implements TheROutputReceiver {

    @NotNull
    private final String myExpectedOutput;

    private int myOutputReceived;

    public MockTheROutputReceiver(@NotNull final String expectedOutput) {
      myExpectedOutput = expectedOutput;
      myOutputReceived = 0;
    }

    @Override
    public void receiveOutput(@NotNull final String output) {
      myOutputReceived++;

      assertEquals(myExpectedOutput, output);
    }

    @Override
    public void receiveError(@NotNull final String error) {
      throw new IllegalStateException("ReceiveError shouldn't be called");
    }
  }
}