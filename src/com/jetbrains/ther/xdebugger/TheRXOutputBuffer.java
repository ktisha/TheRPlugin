package com.jetbrains.ther.xdebugger;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;

class TheRXOutputBuffer implements TheROutputReceiver {

  @NotNull
  private final Queue<String> myMessages = new LinkedList<String>();

  @NotNull
  public Queue<String> getMessages() {
    return myMessages;
  }

  @Override
  public void receive(@NotNull final String message) {
    myMessages.add(message);
  }
}
