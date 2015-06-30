package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRScriptLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.NOP_COMMAND;
import static org.junit.Assert.assertEquals;

public class TheRScriptReaderTest {

  @NotNull
  private static final File DEBUGGER_TEST_DATA_DIR = new File("testData/debugger");

  @Test
  public void read01() throws IOException {
    final String scriptPath = new File(DEBUGGER_TEST_DATA_DIR, "01.r").getAbsolutePath();
    final TheRScriptReader reader = new TheRScriptReader(scriptPath);

    checkCommand(NOP_COMMAND, -1, reader.getCurrentLine());
    checkCommand(NOP_COMMAND, 0, reader.getNextLine());

    reader.advance();

    checkCommand(NOP_COMMAND, 0, reader.getCurrentLine());
    checkCommand("x <- c(1)", 1, reader.getNextLine());

    reader.advance();

    checkCommand("x <- c(1)", 1, reader.getCurrentLine());
    checkCommand("y <- c(2)", 2, reader.getNextLine());

    reader.advance();

    checkCommand("y <- c(2)", 2, reader.getCurrentLine());
    checkCommand(NOP_COMMAND, 3, reader.getNextLine());

    reader.advance();

    checkCommand(NOP_COMMAND, 3, reader.getCurrentLine());
    checkCommand("z <- c(2)", 4, reader.getNextLine());

    reader.advance();

    checkCommand("z <- c(2)", 4, reader.getCurrentLine());
    checkCommand(null, -1, reader.getNextLine());

    reader.advance();

    checkCommand(null, -1, reader.getCurrentLine());
    checkCommand(null, -1, reader.getNextLine());

    reader.close();
  }

  @Test
  public void read02() throws IOException {
    final String scriptPath = new File(DEBUGGER_TEST_DATA_DIR, "02.r").getAbsolutePath();
    final TheRScriptReader reader = new TheRScriptReader(scriptPath);

    checkCommand(NOP_COMMAND, -1, reader.getCurrentLine());
    checkCommand("x <- c(1)", 0, reader.getNextLine());

    reader.advance();

    checkCommand("x <- c(1)", 0, reader.getCurrentLine());
    checkCommand("y <- c(2)", 1, reader.getNextLine());

    reader.advance();

    checkCommand("y <- c(2)", 1, reader.getCurrentLine());
    checkCommand(NOP_COMMAND, 2, reader.getNextLine());

    reader.advance();

    checkCommand(NOP_COMMAND, 2, reader.getCurrentLine());
    checkCommand("z <- c(2)", 3, reader.getNextLine());

    reader.advance();

    checkCommand("z <- c(2)", 3, reader.getCurrentLine());
    checkCommand(null, -1, reader.getNextLine());

    reader.advance();

    checkCommand(null, -1, reader.getCurrentLine());
    checkCommand(null, -1, reader.getNextLine());

    reader.close();
  }

  @Test
  public void read03() throws IOException {
    final String scriptPath = new File(DEBUGGER_TEST_DATA_DIR, "03.r").getAbsolutePath();
    final TheRScriptReader reader = new TheRScriptReader(scriptPath);

    checkCommand(NOP_COMMAND, -1, reader.getCurrentLine());
    checkCommand(NOP_COMMAND, 0, reader.getNextLine());

    reader.advance();

    checkCommand(NOP_COMMAND, 0, reader.getCurrentLine());
    checkCommand(NOP_COMMAND, 1, reader.getNextLine());

    reader.advance();

    checkCommand(NOP_COMMAND, 1, reader.getCurrentLine());
    checkCommand(NOP_COMMAND, 2, reader.getNextLine());

    reader.advance();

    checkCommand(NOP_COMMAND, 2, reader.getCurrentLine());
    checkCommand("x <- c(1)", 3, reader.getNextLine());

    reader.advance();

    checkCommand("x <- c(1)", 3, reader.getCurrentLine());
    checkCommand(null, -1, reader.getNextLine());

    reader.advance();

    checkCommand(null, -1, reader.getCurrentLine());
    checkCommand(null, -1, reader.getNextLine());

    reader.close();
  }

  private void checkCommand(@Nullable final String expectedCommand,
                            final int expectedPosition,
                            @NotNull final TheRScriptLine actualCommand) {
    assertEquals(expectedCommand, actualCommand.getText());
    assertEquals(expectedPosition, actualCommand.getNumber());
  }
}