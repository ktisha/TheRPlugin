package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRScriptCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TheRScriptReaderTest {

  @NotNull
  private static final File DEBUGGER_TEST_DATA_DIR = new File("testData/debugger");

  @Test
  public void read01() throws IOException {
    final String scriptPath = new File(DEBUGGER_TEST_DATA_DIR, "01.r").getAbsolutePath();
    final TheRScriptReader reader = new TheRScriptReader(scriptPath);

    checkCommand(String.valueOf(TheRDebugConstants.PING_COMMAND), -1, reader.getCurrentCommand());
    checkCommand("x <- c(1)", 1, reader.getNextCommand());

    reader.advance();

    checkCommand("x <- c(1)", 1, reader.getCurrentCommand());
    checkCommand("y <- c(2)", 2, reader.getNextCommand());

    reader.advance();

    checkCommand("y <- c(2)", 2, reader.getCurrentCommand());
    checkCommand("z <- c(2)", 4, reader.getNextCommand());

    reader.advance();

    checkCommand("z <- c(2)", 4, reader.getCurrentCommand());
    checkCommand(null, -1, reader.getNextCommand());

    reader.advance();

    checkCommand(null, -1, reader.getCurrentCommand());
    checkCommand(null, -1, reader.getNextCommand());

    reader.close();
  }

  @Test
  public void read02() throws IOException {
    final String scriptPath = new File(DEBUGGER_TEST_DATA_DIR, "02.r").getAbsolutePath();
    final TheRScriptReader reader = new TheRScriptReader(scriptPath);

    checkCommand(String.valueOf(TheRDebugConstants.PING_COMMAND), -1, reader.getCurrentCommand());
    checkCommand("x <- c(1)", 0, reader.getNextCommand());

    reader.advance();

    checkCommand("x <- c(1)", 0, reader.getCurrentCommand());
    checkCommand("y <- c(2)", 1, reader.getNextCommand());

    reader.advance();

    checkCommand("y <- c(2)", 1, reader.getCurrentCommand());
    checkCommand("z <- c(2)", 3, reader.getNextCommand());

    reader.advance();

    checkCommand("z <- c(2)", 3, reader.getCurrentCommand());
    checkCommand(null, -1, reader.getNextCommand());

    reader.advance();

    checkCommand(null, -1, reader.getCurrentCommand());
    checkCommand(null, -1, reader.getNextCommand());

    reader.close();
  }

  @Test
  public void read03() throws IOException {
    final String scriptPath = new File(DEBUGGER_TEST_DATA_DIR, "03.r").getAbsolutePath();
    final TheRScriptReader reader = new TheRScriptReader(scriptPath);

    checkCommand(String.valueOf(TheRDebugConstants.PING_COMMAND), -1, reader.getCurrentCommand());
    checkCommand("x <- c(1)", 3, reader.getNextCommand());

    reader.advance();

    checkCommand("x <- c(1)", 3, reader.getCurrentCommand());
    checkCommand(null, -1, reader.getNextCommand());

    reader.advance();

    checkCommand(null, -1, reader.getCurrentCommand());
    checkCommand(null, -1, reader.getNextCommand());

    reader.close();
  }

  private void checkCommand(@Nullable final String expectedCommand,
                            final int expectedPosition,
                            @NotNull final TheRScriptCommand actualCommand) {
    assertEquals(expectedCommand, actualCommand.getCommand());
    assertEquals(expectedPosition, actualCommand.getPosition());
  }
}