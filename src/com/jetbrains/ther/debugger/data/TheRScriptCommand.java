package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.Nullable;

public class TheRScriptCommand {

  @Nullable
  private final String myCommand;

  private final int myPosition;

  public TheRScriptCommand(@Nullable final String command, final int position) {
    myCommand = command;
    myPosition = position;
  }

  @Nullable
  public String getCommand() {
    return myCommand;
  }

  public int getPosition() {
    return myPosition;
  }
}
