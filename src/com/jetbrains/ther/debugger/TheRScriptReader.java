package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRScriptLine;
import org.jetbrains.annotations.NotNull;

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

    return new TheRScriptLine(line, position);
  }
}
