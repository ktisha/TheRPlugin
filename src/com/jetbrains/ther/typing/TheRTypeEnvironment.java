package com.jetbrains.ther.typing;

import com.jetbrains.ther.typing.types.TheRType;

import java.util.HashMap;
import java.util.Map;

public class TheRTypeEnvironment {
  private Map<String, TheRType> nameToType = new HashMap<String, TheRType>();

  public void addType(String name, TheRType type) {
    nameToType.put(name, type);
  }

  public TheRType getType(String name) {
    return nameToType.get(name);
  }

  public boolean contains(String name) {
    return nameToType.containsKey(name);
  }
}
