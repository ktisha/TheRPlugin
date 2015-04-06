package com.jetbrains.ther.debugger;

import com.intellij.xdebugger.XSourcePosition;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TheRStackFrameData {

  @NotNull
  private final XSourcePosition myPosition;

  @NotNull
  private final Map<String, String> myVarRepresentations;

  @NotNull
  private final Map<String, String> myVarTypes;

  public TheRStackFrameData(@NotNull XSourcePosition position,
                            @NotNull Map<String, String> varRepresentations,
                            @NotNull Map<String, String> varTypes) {
    myPosition = position;
    myVarRepresentations = varRepresentations;
    myVarTypes = varTypes;
  }

  @NotNull
  public XSourcePosition getPosition() {
    return myPosition;
  }

  @NotNull
  public Map<String, String> getVarRepresentations() {
    return myVarRepresentations;
  }

  @NotNull
  public Map<String, String> getVarTypes() {
    return myVarTypes;
  }
}
