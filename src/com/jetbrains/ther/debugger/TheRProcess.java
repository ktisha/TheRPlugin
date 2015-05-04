package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponseAndType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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

    mySender = new TheRProcessSender(myProcess.getOutputStream());
    myReceiver = new TheRProcessReceiver(myProcess.getInputStream(), mySender);

    mySender.send(TheRDebugConstants.BROWSER_COMMAND);
    myReceiver.receive();
  }

  @NotNull
  public TheRProcessResponseAndType execute(@NotNull final String command) throws IOException, InterruptedException {
    mySender.send(command);
    return myReceiver.receive();
  }

  @NotNull
  public TheRProcessResponseAndType execute(final char command) throws IOException, InterruptedException {
    mySender.send(command);
    return myReceiver.receive();
  }

  public void stop() {
    myProcess.destroy();
  }
}
