package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;

public interface TheROutputReceiver {

  void receiveOutput(@NotNull final String output);

  void receiveError(@NotNull final String error);
}
