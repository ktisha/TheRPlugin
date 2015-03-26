package com.jetbrains.ther.typing;

//TODO: create some implementations
public interface TheRType {
  TheRType UNKNOWN = new TheRType() {
    @Override
    public String getName() {
      return "Unknown";
    }

    @Override
    public TheRType resolveType(TheRTypeEnvironment env) {
      return this;
    }
  };

  String getName();

  TheRType resolveType(TheRTypeEnvironment env);
}
