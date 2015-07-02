package com.jetbrains.ther.debugger;

import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.ther.debugger.data.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TheRDebugger {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRDebugger.class);

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRScriptReader myScriptReader;

  @NotNull
  private final TheRStackHandler myStackHandler;

  @NotNull
  private final TheROutput myOutput;

  @NotNull
  private TheRLocation myCurrentLocation;

  /**
   * Constructs new instance of debugger with specified interpreter and script.
   *
   * @param interpreterPath path to interpreter
   * @param scriptPath      path to script
   * @throws IOException          if script couldn't be opened or interpreter couldn't be started
   * @throws InterruptedException if thread was interrupted while waiting interpreter's response
   */
  public TheRDebugger(@NotNull final String interpreterPath, @NotNull final String scriptPath) throws IOException, InterruptedException {
    myProcess = new TheRProcess(interpreterPath);
    myScriptReader = new TheRScriptReader(scriptPath);

    myStackHandler = new TheRStackHandler();
    myStackHandler.addFrame();

    myOutput = new TheROutput();

    myCurrentLocation = new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, 0);
    myStackHandler.updateCurrentFrame(new TheRStackFrame(myCurrentLocation, Collections.<TheRVar>emptyList()));
  }

  /**
   * @return true, or false if the end of the source has been reached
   * @throws IOException          if script couldn't be read or communication with interpreter was broken
   * @throws InterruptedException if thread was interrupted while waiting interpreter's response
   */
  public boolean executeInstruction() throws IOException, InterruptedException {
    myOutput.reset();

    if (myStackHandler.isMain()) {
      if (!executeScriptInstruction()) {
        return false;
      }
    }
    else {
      executeFunctionInstruction();
    }

    myStackHandler
      .updateCurrentFrame(new TheRStackFrame(myCurrentLocation, Collections.unmodifiableList(TheRDebuggerUtils.loadVars(myProcess))));

    return true;
  }

  @NotNull
  public List<TheRStackFrame> getStack() {
    return myStackHandler.getStack();
  }

  @NotNull
  public TheROutput getOutput() {
    return myOutput;
  }

  public void stop() {
    try {
      myScriptReader.close();
    }
    catch (final IOException e) {
      LOGGER.warn(e);
    }

    myProcess.stop();
  }

  private boolean executeScriptInstruction() throws IOException, InterruptedException {
    boolean accepted = false;
    boolean firstLine = true;

    while (!accepted) {
      final TheRScriptLine line = myScriptReader.getCurrentLine();

      if (line.getText() == null) {
        return false;
      }

      if (TheRDebugUtils.isCommentOrSpaces(line.getText()) && firstLine) {
        myCurrentLocation = new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, myScriptReader.getNextLine().getNumber());
        myScriptReader.advance();
        break;
      }

      final TheRProcessResponse response = myProcess.execute(line.getText());

      myCurrentLocation = new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, myScriptReader.getNextLine().getNumber());

      handleResponse(response);

      accepted = response.getType() != TheRProcessResponseType.PLUS;

      myScriptReader.advance();

      firstLine = false;
    }

    return true;
  }

  private void executeFunctionInstruction() throws IOException, InterruptedException {
    // TODO check type
    handleResponse(myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND));
  }

  private void handleResponse(@NotNull final TheRProcessResponse response) throws IOException, InterruptedException {
    if (response.getType() == TheRProcessResponseType.DEBUGGING_IN || response.getType() == TheRProcessResponseType.CONTINUE_TRACE) {
      // TODO check type
      myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND);
      // TODO check type
      myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND);
      // TODO check type
      myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND);
      // TODO check type
      final TheRProcessResponse entryResponse = myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND);

      final String entryText = entryResponse.getText();

      final int firstLineSeparator = entryText.indexOf(TheRDebugConstants.LINE_SEPARATOR);
      final int secondLineSeparator = entryText.indexOf(TheRDebugConstants.LINE_SEPARATOR, firstLineSeparator + 1);

      final String function = entryText.substring(
        firstLineSeparator + "[1] \"".length() + "enter ".length() + 1,
        secondLineSeparator - "\"".length()
      );

      // TODO check type
      final TheRProcessResponse firstInstructionResponse = myProcess.execute(TheRDebugConstants.EXECUTE_AND_STEP_COMMAND);

      final int lineStart = "debug at #".length();
      final int lineEnd = firstInstructionResponse.getText().indexOf(':', lineStart);
      final int line = Integer.parseInt(firstInstructionResponse.getText().substring(lineStart, lineEnd));

      myCurrentLocation = new TheRLocation(function, line - 1);

      if (response.getType() == TheRProcessResponseType.DEBUGGING_IN) {
        myStackHandler.addFrame();
      }
    }

    if (response.getType() == TheRProcessResponseType.END_TRACE) {
      myStackHandler.removeFrame();

      // myCurrentLocation = myStackHandler.getCurrentLocation();
      myCurrentLocation =
        new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, myScriptReader.getCurrentLine().getNumber()); // TODO update
    }

    if (response.getType() == TheRProcessResponseType.RESPONSE_AND_BROWSE) {
      myOutput.setNormalOutput(response.getText());
    }

    if (response.getType() == TheRProcessResponseType.DEBUG_AT) {
      final int lineStart = "debug at #".length();
      final int lineEnd = response.getText().indexOf(':', lineStart);
      final int line = Integer.parseInt(response.getText().substring(lineStart, lineEnd));

      myCurrentLocation = new TheRLocation(myStackHandler.getCurrentLocation().getFunction(), line - 1);
    }
  }
}
