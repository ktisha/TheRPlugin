package com.jetbrains.ther.packages;

import org.jetbrains.annotations.NotNull;

public class TheRPackage {
  @NotNull private final String myName;
  @NotNull private final String myPath;
  @NotNull private final String myVersion;

  public TheRPackage(@NotNull final String name, @NotNull final String path, @NotNull final String version) {
    myName = name;
    myPath = path;
    myVersion = version;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public String getPath() {
    return myPath;
  }

  @NotNull
  public String getVersion() {
    return myVersion;
  }
}
