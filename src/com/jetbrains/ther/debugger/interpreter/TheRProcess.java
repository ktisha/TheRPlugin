package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TheRProcess {

  @NotNull
  private final Process myProcess;

  @NotNull
  private final TheRProcessSender mySender;

  @NotNull
  private final TheRProcessReceiver myReceiver;

  public TheRProcess(@NotNull final String interpreterPath) throws IOException, InterruptedException {
    final ProcessBuilder builder = new ProcessBuilder(
      interpreterPath, TheRDebugConstants.NO_SAVE_PARAMETER, TheRDebugConstants.QUIET_PARAMETER
    );

    myProcess = builder.start();

    mySender = new TheRProcessSender(new OutputStreamWriter(myProcess.getOutputStream()));
    myReceiver = new TheRProcessReceiver(new InputStreamReader(myProcess.getInputStream()));

    initInterpreter();
  }

  @NotNull
  public TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException {
    mySender.send(command);
    return myReceiver.receive();
  }

  public void stop() {
    myProcess.destroy();
  }

  private void initInterpreter() throws IOException, InterruptedException {
    mySender.send(TheRDebugConstants.BROWSER_COMMAND);
    myReceiver.receive();

    mySender.send(TheRDebugConstants.KEEP_SOURCE_COMMAND);
    myReceiver.receive();
  }
}
