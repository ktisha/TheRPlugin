package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MockTheROutputReceiver implements TheROutputReceiver {

  @NotNull
  private final List<String> myOutputs = new ArrayList<String>();

  @NotNull
  private final List<String> myErrors = new ArrayList<String>();

  @Override
  public void receiveOutput(@NotNull final String output) {
    myOutputs.add(output);
  }

  @Override
  public void receiveError(@NotNull final String error) {
    myErrors.add(error);
  }

  @NotNull
  public List<String> getOutputs() {
    return myOutputs;
  }

  @NotNull
  public List<String> getErrors() {
    return myErrors;
  }

  public void reset() {
    myOutputs.clear();
    myErrors.clear();
  }
}
