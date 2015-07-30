package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.mock.MockReader;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.LINE_SEPARATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRProcessReceiverTest {

  @Test
  public void sleepIncreasing() throws TheRDebuggerException {
    final IncreasingMockReader outputReader = new IncreasingMockReader();
    final MockReader errorReader = new MockReader("");

    final TheRProcessReceiver receiver = new TheRProcessReceiver(outputReader, errorReader);

    receiver.receive();

    assertEquals(9, outputReader.getCounter());
    assertEquals(0, errorReader.getCounter());
  }

  @Test
  public void sleepResetting() throws TheRDebuggerException {
    final ResettingMockReader outputReader = new ResettingMockReader();
    final MockReader errorReader = new MockReader("");

    final TheRProcessReceiver receiver = new TheRProcessReceiver(outputReader, errorReader);

    receiver.receive();

    assertEquals(6, outputReader.getCounter());
    assertEquals(0, errorReader.getCounter());
  }

  @Test
  public void responseHandling() throws TheRDebuggerException {
    final MockReader outputReader = new MockReader("ls()\n[1] \"x\"\nBrowse[3]> ");
    final MockReader errorReader = new MockReader("error");

    final TheRProcessReceiver receiver = new TheRProcessReceiver(outputReader, errorReader);
    final TheRProcessResponse response = receiver.receive();

    assertEquals(TheRProcessResponseType.RESPONSE, response.getType());
    assertEquals("[1] \"x\"", response.getOutput());
    assertEquals("error", response.getError());

    assertEquals(1, outputReader.getCounter());
    assertEquals(1, errorReader.getCounter());
  }

  private static class IncreasingMockReader extends Reader {

    private int myCounter = 0;

    private long myTime = 0;
    private long myPrevPause = 1;

    @Override
    public int read(@NotNull final char[] cbuf, final int off, final int len) throws IOException {
      if (myCounter < 8) {
        checkPause();

        myCounter++;

        return 0;
      }

      if (myCounter == 8) {
        myCounter++;

        return doRead(cbuf);
      }

      throw new IllegalStateException("Unexpected read");
    }

    @Override
    public void close() throws IOException {
    }

    public int getCounter() {
      return myCounter;
    }

    private void checkPause() {
      if (myCounter == 0) {
        myTime = System.currentTimeMillis();
      }
      else {
        final long currentTime = System.currentTimeMillis();
        final long pause = currentTime - myTime;

        assertTrue(pause > myPrevPause);

        myPrevPause = pause;
        myTime = currentTime;
      }
    }

    private int doRead(@NotNull final char[] cbuf) {
      int index = 0;

      for (; index < LINE_SEPARATOR.length(); index++) {
        cbuf[index] = LINE_SEPARATOR.charAt(index);
      }

      cbuf[index] = '+';
      cbuf[index + 1] = ' ';

      return LINE_SEPARATOR.length() + 2;
    }
  }

  private static class ResettingMockReader extends Reader {

    private int myCounter = 0;

    private long myTime = 0;
    private long myPrevPause = 1;

    @Override
    public int read(@NotNull final char[] cbuf, final int off, final int len) throws IOException {
      if (myCounter < 3) {
        checkPause();

        myCounter++;

        return 0;
      }

      if (myCounter == 3) {
        myCounter++;

        return doFirstRead(cbuf);
      }

      if (myCounter == 4) {
        myCounter++;

        myTime = System.currentTimeMillis();

        return 0;
      }

      if (myCounter == 5) {
        myCounter++;

        return doSecondRead(cbuf, off);
      }

      throw new IllegalStateException("Unexpected read");
    }

    @Override
    public void close() throws IOException {
    }

    public int getCounter() {
      return myCounter;
    }

    private int doFirstRead(@NotNull final char[] cbuf) {
      int index = 0;

      for (; index < LINE_SEPARATOR.length(); index++) {
        cbuf[index] = LINE_SEPARATOR.charAt(index);
      }


      return LINE_SEPARATOR.length();
    }

    private int doSecondRead(@NotNull final char[] cbuf, final int off) {
      final long currentTime = System.currentTimeMillis();
      final long pause = currentTime - myTime;

      assertTrue(pause < myPrevPause);

      cbuf[off] = '+';
      cbuf[off + 1] = ' ';

      return 2;
    }

    private void checkPause() {
      if (myCounter == 0) {
        myTime = System.currentTimeMillis();
      }
      else {
        final long currentTime = System.currentTimeMillis();
        final long pause = currentTime - myTime;

        assertTrue(pause > myPrevPause);

        myPrevPause = pause;
        myTime = currentTime;
      }
    }
  }
}