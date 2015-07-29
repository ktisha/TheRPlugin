package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRScriptLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.NOP_COMMAND;
import static org.junit.Assert.assertEquals;

public class TheRScriptReaderImplTest {

  @NotNull
  private static final File DEBUGGER_TEST_DATA_DIR = new File("testData/debugger");

  @Test
  public void read01() throws IOException {
    final String scriptPath = new File(DEBUGGER_TEST_DATA_DIR, "01.r").getAbsolutePath();
    final TheRScriptReader reader = new TheRScriptReaderImpl(scriptPath);

    checkLine(NOP_COMMAND, -1, reader.getCurrentLine());

    reader.advance();

    checkLine("", 0, reader.getCurrentLine());

    reader.advance();

    checkLine("x <- c(1)", 1, reader.getCurrentLine());

    reader.advance();

    checkLine("y <- c(2)", 2, reader.getCurrentLine());

    reader.advance();

    checkLine("", 3, reader.getCurrentLine());

    reader.advance();

    checkLine("z <- c(2)", 4, reader.getCurrentLine());

    reader.advance();

    checkLine(null, -1, reader.getCurrentLine());

    reader.close();
  }

  @Test
  public void read02() throws IOException {
    final String scriptPath = new File(DEBUGGER_TEST_DATA_DIR, "02.r").getAbsolutePath();
    final TheRScriptReader reader = new TheRScriptReaderImpl(scriptPath);

    checkLine(NOP_COMMAND, -1, reader.getCurrentLine());

    reader.advance();

    checkLine("x <- c(1)", 0, reader.getCurrentLine());

    reader.advance();

    checkLine("y <- c(2)", 1, reader.getCurrentLine());

    reader.advance();

    checkLine("", 2, reader.getCurrentLine());

    reader.advance();

    checkLine("z <- c(2)", 3, reader.getCurrentLine());

    reader.advance();

    checkLine(null, -1, reader.getCurrentLine());

    reader.close();
  }

  @Test
  public void read03() throws IOException {
    final String scriptPath = new File(DEBUGGER_TEST_DATA_DIR, "03.r").getAbsolutePath();
    final TheRScriptReader reader = new TheRScriptReaderImpl(scriptPath);

    checkLine(NOP_COMMAND, -1, reader.getCurrentLine());

    reader.advance();

    checkLine("# just comment", 0, reader.getCurrentLine());

    reader.advance();

    checkLine("    # spaces and comment", 1, reader.getCurrentLine());

    reader.advance();

    checkLine("", 2, reader.getCurrentLine());

    reader.advance();

    checkLine("x <- c(1)", 3, reader.getCurrentLine());

    reader.advance();

    checkLine(null, -1, reader.getCurrentLine());

    reader.close();
  }

  private void checkLine(@Nullable final String expectedText,
                         final int expectedPosition,
                         @NotNull final TheRScriptLine actualLine) {
    assertEquals(expectedText, actualLine.getText());
    assertEquals(expectedPosition, actualLine.getNumber());
  }
}