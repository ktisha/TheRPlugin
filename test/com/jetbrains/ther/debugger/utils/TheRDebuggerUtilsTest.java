package com.jetbrains.ther.debugger.utils;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.RESPONSE;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.START_TRACE;
import static com.jetbrains.ther.debugger.utils.TheRDebuggerUtils.*;
import static org.junit.Assert.*;

public class TheRDebuggerUtilsTest {

  @Test
  public void noVarsLoading() throws IOException, InterruptedException {
    final TheRProcess process = new MockTheRProcess("character(0)", RESPONSE);
    final TheRLoadableVarHandler handler = new NoVarsTheRLoadableVarHandler();

    assertTrue(
      TheRDebuggerUtils.loadVars(
        process,
        handler
      ).isEmpty()
    );
  }

  @Test
  public void varsLoading() throws IOException, InterruptedException {
    final TheRProcess process = new VarsTheRProcess();
    final TheRLoadableVarHandler handler = new VarsTheRLoadableVarHandler();

    final List<TheRVar> actual = TheRDebuggerUtils.loadVars(
      process,
      handler
    );

    final List<TheRVar> expected = Collections.singletonList(
      new TheRVar("x", "newTypeX", "newValueX")
    );

    assertEquals(expected, actual);
  }

  @Test
  public void functionNameLoading() throws IOException, InterruptedException {
    final TheRProcess process = new FunctionNameTheRProcess();

    assertEquals(
      "abc",
      TheRDebuggerUtils.loadFunctionName(process)
    );
  }

  @Test
  public void commentChecking() {
    assertTrue(isCommentOrSpaces(" # abc "));
  }

  @Test
  public void spacesChecking() {
    assertTrue(isCommentOrSpaces("  "));
  }

  @Test
  public void nullChecking() {
    assertFalse(isCommentOrSpaces(null));
  }

  @Test
  public void ordinaryChecking() {
    assertFalse(isCommentOrSpaces(" abc "));
  }

  @Test
  public void oneLineNextLineBegin() {
    final String line = "abc";

    assertEquals(line.length(), findNextLineBegin(line, 0));
  }

  @Test
  public void justLineBreaksNextLineBegin() {
    final String text = "\n\n\n\n\n";

    assertEquals(text.length(), findNextLineBegin(text, 0));
  }

  @Test
  public void lineBreakPointerNextLineBegin() {
    final String text = "abc\n\ndef";

    assertEquals(5, findNextLineBegin(text, 3));
  }

  @Test
  public void ordinaryNextLineBegin() {
    final String text = "abc\ndef";

    assertEquals(4, findNextLineBegin(text, 0));
  }

  @Test
  public void oneLineCurrentLineEnd() {
    final String line = "abc";

    assertEquals(line.length(), findCurrentLineEnd(line, 0));
  }

  @Test
  public void justLineBreaksCurrentLineEnd() {
    final String text = "\n\n\n\n";

    assertEquals(0, findCurrentLineEnd(text, 0));
  }

  @Test
  public void lineBreakPointerCurrentLineEnd() {
    final String text = "abc\n\ndef";

    assertEquals(3, findCurrentLineEnd(text, 3));
  }

  @Test
  public void ordinaryCurrentLineEnd() {
    final String text = "abc\ndef";

    assertEquals(3, findCurrentLineEnd(text, 0));
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
      return new TheRProcessResponse(myText, myType, TextRange.EMPTY_RANGE);
    }

    @Override
    public void stop() {
    }
  }

  private static class NoVarsTheRLoadableVarHandler implements TheRLoadableVarHandler {

    @Nullable
    @Override
    public String handleType(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type)
      throws IOException, InterruptedException {
      throw new IllegalStateException("HandleType shouldn't be called");
    }

    @NotNull
    @Override
    public String handleValue(@NotNull final String var,
                              @NotNull final String type,
                              @NotNull final String value) {
      throw new IllegalStateException("HandleValue shouldn't be called");
    }
  }

  private static class VarsTheRProcess extends TheRProcess {

    private boolean myIsXTypeAsked = false;
    private boolean myIsYTypeAsked = false;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException {
      if (command.equals(TheRDebugConstants.LS_COMMAND)) {
        return new TheRProcessResponse(
          "[1] \"x\"\n[2] \"y\"",
          RESPONSE,
          TextRange.EMPTY_RANGE
        );
      }

      if (command.equals(TheRDebugConstants.TYPEOF_COMMAND + "(x)")) {
        myIsXTypeAsked = true;

        return new TheRProcessResponse(
          "typeX",
          RESPONSE,
          TextRange.EMPTY_RANGE
        );
      }

      if (command.equals(TheRDebugConstants.TYPEOF_COMMAND + "(y)")) {
        myIsYTypeAsked = true;

        return new TheRProcessResponse(
          "typeY",
          RESPONSE,
          TextRange.EMPTY_RANGE
        );
      }

      if (command.equals("x")) {
        if (!myIsXTypeAsked) {
          fail("Type should be asked before value");
        }

        return new TheRProcessResponse(
          "valueX",
          RESPONSE,
          TextRange.EMPTY_RANGE
        );
      }

      if (command.equals("y")) {
        if (!myIsYTypeAsked) {
          fail("Type should be asked before value");
        }

        fail("Type of \"y\" shouldn't be asked");
      }

      throw new IllegalArgumentException("Unexpected command");
    }

    @Override
    public void stop() {
    }
  }

  private static class VarsTheRLoadableVarHandler implements TheRLoadableVarHandler {

    private boolean myIsXTypeAsked = false;
    private boolean myIsYTypeAsked = false;

    @Nullable
    @Override
    public String handleType(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type)
      throws IOException, InterruptedException {
      if (var.equals("x")) {
        myIsXTypeAsked = true;

        return "newTypeX";
      }

      if (var.equals("y")) {
        myIsYTypeAsked = true;

        return null;
      }

      throw new IllegalArgumentException("Unexpected var");
    }

    @NotNull
    @Override
    public String handleValue(@NotNull final String var,
                              @NotNull final String type,
                              @NotNull final String value) {
      if (var.equals("x")) {
        if (!myIsXTypeAsked) {
          fail("Type should be handled before value");
        }

        return "newValueX";
      }

      if (var.equals("y")) {
        if (!myIsYTypeAsked) {
          fail("Type should be handled before value");
        }

        fail("Type of \"y\" shouldn't be handled");
      }

      throw new IllegalArgumentException("Unexpected var");
    }
  }

  private static class FunctionNameTheRProcess extends TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException {
      if (myCounter < 3) {
        myCounter++;

        return new TheRProcessResponse(
          "text",
          RESPONSE,
          TextRange.EMPTY_RANGE
        );
      }

      if (myCounter == 3) {
        myCounter++;

        return new TheRProcessResponse(
          "Tracing abc(c(1:3)) on entry\n" +
          "[1] \"enter abc\"\n" +
          "debug: {\n" +
          "    x^2\n" +
          "}",
          START_TRACE,
          TextRange.EMPTY_RANGE
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }
  }
}