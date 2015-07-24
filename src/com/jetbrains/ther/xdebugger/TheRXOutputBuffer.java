package com.jetbrains.ther.xdebugger;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;

class TheRXOutputBuffer implements TheROutputReceiver {

  @NotNull
  private final Queue<Entry> myMessages = new LinkedList<Entry>();

  @NotNull
  public Queue<Entry> getMessages() {
    return myMessages;
  }

  @Override
  public void receiveOutput(@NotNull final String output) {
    myMessages.add(
      new Entry(
        output,
        ConsoleViewContentType.NORMAL_OUTPUT
      )
    );
  }

  @Override
  public void receiveError(@NotNull final String error) {
    myMessages.add(
      new Entry(
        error,
        ConsoleViewContentType.ERROR_OUTPUT
      )
    );
  }

  public static class Entry {

    @NotNull
    private final String myText;

    @NotNull
    private final ConsoleViewContentType myType;

    public Entry(@NotNull final String text, @NotNull final ConsoleViewContentType type) {
      myText = text;
      myType = type;
    }

    @NotNull
    public String getText() {
      return myText;
    }

    @NotNull
    public ConsoleViewContentType getType() {
      return myType;
    }
  }
}
