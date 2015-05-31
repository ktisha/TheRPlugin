package com.jetbrains.ther.packages;


public class TheRRepository {
  private final String myUrl;


  public TheRRepository(String url) {
    this.myUrl = url;
  }

  public String getUrl() {
    return myUrl;
  }

  @Override
  public String toString() {
    return myUrl;
  }
}
