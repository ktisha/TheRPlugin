package com.jetbrains.ther.debugger.intellij;

import com.intellij.xdebugger.XSourcePosition;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TheRStackFrameData {

  @NotNull
  private final XSourcePosition myPosition;

  @NotNull
  private final Map<String, String> myVarTypes;

  @NotNull
  private final Map<String, String> myVarRepresentations;

  public TheRStackFrameData(@NotNull final XSourcePosition position,
                            @NotNull final Map<String, String> varTypes,
                            @NotNull final Map<String, String> varRepresentations) {
    myPosition = position;
    myVarTypes = varTypes;
    myVarRepresentations = varRepresentations;
  }

  @NotNull
  public XSourcePosition getPosition() {
    return myPosition;
  }

  @NotNull
  public Map<String, String> getVarTypes() {
    return myVarTypes;
  }

  @NotNull
  public Map<String, String> getVarRepresentations() {
    return myVarRepresentations;
  }
}
