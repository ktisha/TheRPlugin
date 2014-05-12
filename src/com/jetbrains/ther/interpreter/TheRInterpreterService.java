package com.jetbrains.ther.interpreter;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(
  name = "TheRInterpreterService",
  storages = {
    @Storage(
      file = StoragePathMacros.APP_CONFIG + "/rInterpreterSettings.xml"
    )}
)
public class TheRInterpreterService implements PersistentStateComponent<TheRInterpreterService> {
  public String INTERPRETER_PATH = "";
  public String INTERPRETER_SOURCES_PATH = "";

  public static TheRInterpreterService getInstance() {
    return ServiceManager.getService(TheRInterpreterService.class);
  }

  @Override
  public TheRInterpreterService getState() {
    return this;
  }

  @Override
  public void loadState(TheRInterpreterService state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public String getInterpreterPath() {
    return INTERPRETER_PATH;
  }

  public void setInterpreterPath(@NotNull final String interpreterPath) {
    INTERPRETER_PATH = interpreterPath;
  }

  public String getSourcesPath() {
    return INTERPRETER_SOURCES_PATH;
  }

  public void setSourcesPath(@NotNull final String interpreterSourcesPath) {
    INTERPRETER_SOURCES_PATH = interpreterSourcesPath;
  }
}
