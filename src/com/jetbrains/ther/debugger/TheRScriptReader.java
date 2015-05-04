package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TheRScriptReader {

  @NotNull
  private final BufferedReader myReader;

  private int myCurrentPosition;

  private int myNextPosition;

  @Nullable
  private String myCachedCommand;

  public TheRScriptReader(@NotNull final String scriptPath) throws FileNotFoundException {
    myReader = new BufferedReader(new FileReader(scriptPath));

    myCurrentPosition = 0;
    myNextPosition = 0;
  }

  @Nullable
  public String getNextCommand() throws IOException {
    final String result = calculateCurrentCommand(myCachedCommand);

    myCachedCommand = calculateNextCommand();

    return result;
  }

  public int getCurrentPosition() {
    return myCurrentPosition;
  }

  public int getNextPosition() {
    return myNextPosition;
  }

  public void close() throws IOException {
    myReader.close();
  }

  @Nullable
  private String calculateCurrentCommand(@Nullable final String cachedCommand) throws IOException {
    String result = cachedCommand;

    if (result == null) {
      do {
        result = myReader.readLine();
        myCurrentPosition++;

        if (result == null) {
          return null;
        }
      }
      while (isCommentOrSpaces(result));
    }

    return result;
  }

  @Nullable
  private String calculateNextCommand() throws IOException {
    String result;

    do {
      result = myReader.readLine();
      myNextPosition++;

      if (result == null) {
        return null;
      }
    }
    while (isCommentOrSpaces(result));

    return result;
  }

  private boolean isCommentOrSpaces(@NotNull final String line) {
    for (int i = 0; i < line.length(); i++) {
      if (StringUtil.isWhiteSpace(line.charAt(i))) {
        continue;
      }

      return line.charAt(i) == TheRDebugConstants.COMMENT_SYMBOL;
    }

    return true;
  }
}
