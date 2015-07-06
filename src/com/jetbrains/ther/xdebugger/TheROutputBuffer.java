package com.jetbrains.ther.xdebugger;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;

public interface TheROutputBuffer extends TheROutputReceiver {

  @NotNull
  Queue<String> getMessages();
}
