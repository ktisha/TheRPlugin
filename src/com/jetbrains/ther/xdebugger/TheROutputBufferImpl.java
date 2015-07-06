package com.jetbrains.ther.xdebugger;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;

public class TheROutputBufferImpl implements TheROutputBuffer {

  @NotNull
  private final LinkedList<String> myMessages = new LinkedList<String>();

  @NotNull
  @Override
  public Queue<String> getMessages() {
    return myMessages;
  }

  @Override
  public void receive(@NotNull final String message) {
    myMessages.add(message);
  }
}
