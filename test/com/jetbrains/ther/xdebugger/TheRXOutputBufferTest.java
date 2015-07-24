package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ui.ConsoleViewContentType;
import org.junit.Test;

import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRXOutputBufferTest {

  @Test
  public void ordinary() {
    final TheRXOutputBuffer outputBuffer = new TheRXOutputBuffer();

    outputBuffer.receiveOutput("abc");
    outputBuffer.receiveError("ghi");
    outputBuffer.receiveOutput("def");

    final Queue<TheRXOutputBuffer.Entry> messages = outputBuffer.getMessages();

    assertEquals(3, messages.size());

    TheRXOutputBuffer.Entry message = messages.poll();
    assertEquals("abc", message.getText());
    assertEquals(ConsoleViewContentType.NORMAL_OUTPUT, message.getType());

    assertEquals(2, messages.size());

    message = messages.poll();
    assertEquals("ghi", message.getText());
    assertEquals(ConsoleViewContentType.ERROR_OUTPUT, message.getType());

    assertEquals(1, messages.size());

    message = messages.poll();
    assertEquals("def", message.getText());
    assertEquals(ConsoleViewContentType.NORMAL_OUTPUT, message.getType());

    assertTrue(messages.isEmpty());
  }
}