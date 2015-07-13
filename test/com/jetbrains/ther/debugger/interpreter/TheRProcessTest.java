package com.jetbrains.ther.debugger.interpreter;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import com.jetbrains.ther.debugger.mock.AlwaysSameResponseTheRProcess;
import org.junit.Test;

import java.io.IOException;

import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.RESPONSE;
import static org.junit.Assert.assertEquals;

public class TheRProcessTest {

  @Test(expected = IOException.class)
  public void invalidCommandExecuting() throws IOException, InterruptedException {
    final String text = "abc";

    final TheRProcess process = new AlwaysSameResponseTheRProcess(text, RESPONSE, TextRange.allOf(text));

    process.execute(
      "def",
      TheRProcessResponseType.PLUS
    );
  }

  @Test
  public void correctCommandExecuting() throws IOException, InterruptedException {
    final String text = "abc";

    final TheRProcess process = new AlwaysSameResponseTheRProcess(text, RESPONSE, TextRange.allOf(text));

    assertEquals(
      text,
      process.execute(
        "def",
        RESPONSE
      )
    );
  }
}