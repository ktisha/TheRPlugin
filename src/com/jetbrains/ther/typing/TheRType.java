package com.jetbrains.ther.typing;

//TODO: create some implementations
public interface TheRType {
  TheRType UNKNOWN = new TheRType() {
    @Override
    public String getName() {
      return "Unknown";
    }
  };

  String getName();
}
