package com.jetbrains.ther.debugger.utils;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.mock.AlwaysSameResponseTheRProcess;
import com.jetbrains.ther.debugger.mock.IllegalTheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.RESPONSE;
import static com.jetbrains.ther.debugger.utils.TheRDebuggerUtils.*;
import static org.junit.Assert.*;

public class TheRDebuggerUtilsTest {

  @Test
  public void noVarsLoading() throws IOException, InterruptedException {
    final String text = "character(0)";

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(text, RESPONSE, TextRange.allOf(text));
    final TheRLoadableVarHandler handler = new IllegalTheRLoadableVarHandler();

    assertTrue(
      TheRDebuggerUtils.loadVars(
        process,
        handler
      ).isEmpty()
    );

    assertEquals(1, process.getExecuteCalled());
  }

  @Test
  public void varsLoading() throws IOException, InterruptedException {
    final VarsTheRProcess process = new VarsTheRProcess();
    final VarsTheRLoadableVarHandler handler = new VarsTheRLoadableVarHandler();

    final List<TheRVar> actual = TheRDebuggerUtils.loadVars(
      process,
      handler
    );

    final List<TheRVar> expected = Collections.singletonList(
      new TheRVar("x", "newTypeX", "newValueX")
    );

    assertEquals(expected, actual);

    assertTrue(process.isLsAsked());
    assertTrue(process.isXTypeAsked());
    assertTrue(process.isYTypeAsked());
    assertTrue(process.isXValueAsked());

    assertTrue(handler.isXTypeAsked());
    assertTrue(handler.isYTypeAsked());
    assertTrue(handler.isXValueAsked());
  }

  @Test
  public void functionNameExtracting() throws IOException, InterruptedException {
    assertEquals(
      "abc",
      extractFunctionName(
        "Tracing abc(c(1:3)) on entry\n" +
        "[1] \"enter abc\"\n" +
        "debug: {\n" +
        "    x^2\n" +
        "}"
      )
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

  private static class VarsTheRProcess extends TheRProcess {

    private boolean myLsAsked = false;

    private boolean myXTypeAsked = false;
    private boolean myYTypeAsked = false;

    private boolean myXValueAsked = false;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException {
      if (command.equals(TheRDebugConstants.LS_COMMAND)) {
        myLsAsked = true;

        return new TheRProcessResponse(
          "[1] \"x\"\n[2] \"y\"",
          RESPONSE,
          TextRange.EMPTY_RANGE
        );
      }

      if (command.equals(TheRDebugConstants.TYPEOF_COMMAND + "(x)")) {
        myXTypeAsked = true;

        return new TheRProcessResponse(
          "typeX",
          RESPONSE,
          TextRange.EMPTY_RANGE
        );
      }

      if (command.equals(TheRDebugConstants.TYPEOF_COMMAND + "(y)")) {
        myYTypeAsked = true;

        return new TheRProcessResponse(
          "typeY",
          RESPONSE,
          TextRange.EMPTY_RANGE
        );
      }

      if (command.equals("x")) {
        if (!myXTypeAsked) {
          throw new IllegalStateException("Type should be asked before value");
        }

        myXValueAsked = true;

        return new TheRProcessResponse(
          "valueX",
          RESPONSE,
          TextRange.EMPTY_RANGE
        );
      }

      if (command.equals("y")) {
        if (!myYTypeAsked) {
          throw new IllegalStateException("Type should be asked before value");
        }

        throw new IllegalStateException("Type of \"y\" shouldn't be asked");
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }

    public boolean isLsAsked() {
      return myLsAsked;
    }

    public boolean isXTypeAsked() {
      return myXTypeAsked;
    }

    public boolean isYTypeAsked() {
      return myYTypeAsked;
    }

    public boolean isXValueAsked() {
      return myXValueAsked;
    }
  }

  private static class VarsTheRLoadableVarHandler implements TheRLoadableVarHandler {

    private boolean myXTypeAsked = false;
    private boolean myYTypeAsked = false;

    private boolean myXValueAsked = false;

    @Nullable
    @Override
    public String handleType(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type)
      throws IOException, InterruptedException {
      if (var.equals("x")) {
        myXTypeAsked = true;

        return "newTypeX";
      }

      if (var.equals("y")) {
        myYTypeAsked = true;

        return null;
      }

      throw new IllegalStateException("Unexpected var");
    }

    @NotNull
    @Override
    public String handleValue(@NotNull final String var,
                              @NotNull final String type,
                              @NotNull final String value) {
      if (var.equals("x")) {
        if (!myXTypeAsked) {
          throw new IllegalStateException("Type should be handled before value");
        }

        myXValueAsked = true;

        return "newValueX";
      }

      if (var.equals("y")) {
        if (!myYTypeAsked) {
          throw new IllegalStateException("Type should be handled before value");
        }

        throw new IllegalStateException("Type of \"y\" shouldn't be handled");
      }

      throw new IllegalStateException("Unexpected var");
    }

    public boolean isXTypeAsked() {
      return myXTypeAsked;
    }

    public boolean isYTypeAsked() {
      return myYTypeAsked;
    }

    public boolean isXValueAsked() {
      return myXValueAsked;
    }
  }
}