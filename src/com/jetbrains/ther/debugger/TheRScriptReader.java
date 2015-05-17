package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRScriptCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TheRScriptReader {

  @NotNull
  private final BufferedReader myReader;

  @NotNull
  private TheRScriptCommand myCurrentCommand;

  @NotNull
  private TheRScriptCommand myNextCommand;

  public TheRScriptReader(@NotNull final String scriptPath) throws IOException {
    myReader = new BufferedReader(new FileReader(scriptPath));

    myCurrentCommand = new TheRScriptCommand(String.valueOf(TheRDebugConstants.PING_COMMAND), -1);
    myNextCommand = readNextCommand(-1);
  }

  public void advance() throws IOException {
    myCurrentCommand = myNextCommand;

    if (myCurrentCommand.getCommand() != null) {
      myNextCommand = readNextCommand(myCurrentCommand.getPosition());
    }
  }

  @NotNull
  public TheRScriptCommand getCurrentCommand() {
    return myCurrentCommand;
  }

  @NotNull
  public TheRScriptCommand getNextCommand() {
    return myNextCommand;
  }

  public void close() throws IOException {
    myReader.close();
  }

  @NotNull
  private TheRScriptCommand readNextCommand(final int currentPosition) throws IOException {
    String result;
    int position = currentPosition;

    do {
      result = myReader.readLine();
      position++;
    }
    while (isCommentOrSpaces(result));

    return new TheRScriptCommand(result, position);
  }

  private boolean isCommentOrSpaces(@Nullable final String line) {
    if (line == null) {
      return false;
    }

    for (int i = 0; i < line.length(); i++) {
      if (StringUtil.isWhiteSpace(line.charAt(i))) {
        continue;
      }

      return line.charAt(i) == TheRDebugConstants.COMMENT_SYMBOL;
    }

    return true;
  }
}
