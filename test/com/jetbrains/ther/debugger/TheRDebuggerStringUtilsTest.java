package com.jetbrains.ther.debugger;

import org.junit.Test;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.*;
import static org.junit.Assert.*;

public class TheRDebuggerStringUtilsTest {

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
}