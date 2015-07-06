package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.RESPONSE;
import static org.junit.Assert.assertEquals;

public class TheRProcessTest {

  @Test(expected = IOException.class)
  public void invalidCommandExecuting() throws IOException, InterruptedException {
    final TheRProcess process = new MockTheRProcess("abc", RESPONSE);

    process.execute(
      "def",
      TheRProcessResponseType.PLUS
    );
  }

  @Test
  public void correctCommandExecuting() throws IOException, InterruptedException {
    final TheRProcess process = new MockTheRProcess("abc", RESPONSE);

    assertEquals(
      "abc",
      process.execute(
        "def",
        RESPONSE
      )
    );
  }

  private static class MockTheRProcess extends TheRProcess {

    @NotNull
    private final String myText;

    @NotNull
    private final TheRProcessResponseType myType;

    public MockTheRProcess(@NotNull final String text, @NotNull final TheRProcessResponseType type) {
      myText = text;
      myType = type;
    }

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException {
      return new TheRProcessResponse(myText, myType);
    }

    @Override
    public void stop() {
    }
  }
}