package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRScriptLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class TheRScriptReader {

  @NotNull
  private final LineNumberReader myReader;

  @NotNull
  private TheRScriptLine myCurrentLine;

  @NotNull
  private TheRScriptLine myNextLine;

  public TheRScriptReader(@NotNull final String scriptPath) throws IOException {
    myReader = new LineNumberReader(new FileReader(scriptPath));

    myCurrentLine = new TheRScriptLine(TheRDebugConstants.NOP_COMMAND, -1);
    myNextLine = readNextLine();
  }

  public void advance() throws IOException {
    myCurrentLine = myNextLine;

    if (myCurrentLine.getText() != null) {
      myNextLine = readNextLine();
    }
  }

  @NotNull
  public TheRScriptLine getCurrentLine() {
    return myCurrentLine;
  }

  @NotNull
  public TheRScriptLine getNextLine() {
    return myNextLine;
  }

  public void close() throws IOException {
    myReader.close();
  }

  @NotNull
  private TheRScriptLine readNextLine() throws IOException {
    final String line = myReader.readLine();

    final int position = (line == null) ? -1 : myReader.getLineNumber() - 1;

    final String result = isCommentOrSpaces(line) ? TheRDebugConstants.NOP_COMMAND : line;

    return new TheRScriptLine(result, position);
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
