package com.jetbrains.ther.debugger.interpreter;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.mock.AlwaysSameResponseTheRProcess;
import com.jetbrains.ther.debugger.mock.IllegalTheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.RESPONSE;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.execute;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.loadVars;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRProcessUtilsTest {

  @Test(expected = TheRDebuggerException.class)
  public void invalidCommandExecuting() throws TheRDebuggerException {
    final String text = "abc";
    final TheRProcess process = new AlwaysSameResponseTheRProcess(text, RESPONSE, TextRange.allOf(text), "");

    execute(
      process,
      "def",
      TheRProcessResponseType.PLUS
    );
  }

  @Test
  public void correctCommandExecuting() throws TheRDebuggerException {
    final String text = "abc";
    final TheRProcess process = new AlwaysSameResponseTheRProcess(text, RESPONSE, TextRange.allOf(text), "");

    assertEquals(
      text,
      execute(
        process,
        "def",
        RESPONSE
      )
    );
  }

  @Test
  public void noVarsLoading() throws TheRDebuggerException {
    final String text = "character(0)";
    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(text, RESPONSE, TextRange.allOf(text), "");

    final TheRLoadableVarHandler handler = new IllegalTheRLoadableVarHandler();

    assertTrue(
      loadVars(
        process,
        handler
      ).isEmpty()
    );

    assertEquals(1, process.getExecuteCalled());
  }

  @Test
  public void varsLoading() throws TheRDebuggerException {
    final VarsTheRProcess process = new VarsTheRProcess();
    final VarsTheRLoadableVarHandler handler = new VarsTheRLoadableVarHandler();

    final List<TheRVar> actual = loadVars(
      process,
      handler
    );

    final List<TheRVar> expected = Collections.singletonList(
      new TheRVar("x", "newTypeX", "newValueX")
    );

    assertEquals(expected, actual);

    assertTrue(process.isComplete());
    assertTrue(handler.isComplete());
  }

  private static class VarsTheRProcess implements TheRProcess {

    private boolean myLsAsked = false;

    private boolean myXTypeAsked = false;
    private boolean myYTypeAsked = false;

    private boolean myXValueAsked = false;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_COMMAND)) {
        myLsAsked = true;

        return new TheRProcessResponse(
          "[1] \"x\"\n[2] \"y\"",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (command.equals(TYPEOF_COMMAND + "(x)")) {
        myXTypeAsked = true;

        return new TheRProcessResponse(
          "typeX",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (command.equals(TYPEOF_COMMAND + "(y)")) {
        myYTypeAsked = true;

        return new TheRProcessResponse(
          "typeY",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (command.equals(PRINT_COMMAND + "(x)")) {
        if (!myXTypeAsked) {
          throw new IllegalStateException("Type should be asked before value");
        }

        myXValueAsked = true;

        return new TheRProcessResponse(
          "valueX",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (command.equals(PRINT_COMMAND + "(y)")) {
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

    public boolean isComplete() {
      return myLsAsked && myXTypeAsked && myYTypeAsked && myXValueAsked;
    }
  }

  private static class VarsTheRLoadableVarHandler implements TheRLoadableVarHandler {

    private boolean myXTypeAsked = false;
    private boolean myYTypeAsked = false;

    private boolean myXValueAsked = false;

    @Nullable
    @Override
    public String handleType(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type)
      throws TheRDebuggerException {
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

    public boolean isComplete() {
      return myXTypeAsked && myYTypeAsked && myXValueAsked;
    }
  }
}