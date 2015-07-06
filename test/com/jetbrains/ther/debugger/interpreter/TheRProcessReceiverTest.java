package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.LINE_SEPARATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRProcessReceiverTest {

  @Test
  public void sleepIncreasing() throws IOException, InterruptedException {
    final TheRProcessReceiver receiver = new TheRProcessReceiver(new IncreasingMockReader());

    receiver.receive();
  }

  @Test
  public void sleepResetting() throws IOException, InterruptedException {
    final TheRProcessReceiver receiver = new TheRProcessReceiver(new ResettingMockReader());

    receiver.receive();
  }

  @Test
  public void responseHandling() throws IOException, InterruptedException {
    final TheRProcessReceiver receiver = new TheRProcessReceiver(new MockReader());

    final TheRProcessResponse response = receiver.receive();

    assertEquals(TheRProcessResponseType.RESPONSE, response.getType());
    assertEquals("[1] \"x\"", response.getText());
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

      return doRead(cbuf);
    }

    @Override
    public void close() throws IOException {
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

      return doSecondRead(cbuf, off);
    }

    @Override
    public void close() throws IOException {
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

  private static class MockReader extends Reader {

    @Override
    public int read(@NotNull final char[] cbuf, final int off, final int len) throws IOException {
      final String response = "ls()\n[1] \"x\"\nBrowse[3]> ";

      for (int index = 0; index < response.length(); index++) {
        cbuf[index] = response.charAt(index);
      }

      return response.length();
    }

    @Override
    public void close() throws IOException {

    }
  }
}