package com.jetbrains.ther.xdebugger;

import org.junit.Test;

import java.util.Queue;

import static org.junit.Assert.assertEquals;

public class TheROutputBufferTest {

  @Test
  public void ordinary() {
    final TheROutputBuffer outputBuffer = new TheROutputBuffer();

    outputBuffer.receive("abc");
    outputBuffer.receive("def");

    final Queue<String> messages = outputBuffer.getMessages();

    assertEquals(2, messages.size());
    assertEquals("abc", messages.poll());

    assertEquals(1, messages.size());
    assertEquals("def", messages.poll());
  }
}