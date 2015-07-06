package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;

public interface TheROutputReceiver {

  void receive(@NotNull final String message);
}
