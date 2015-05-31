package com.jetbrains.ther.packages;


public class TheRDefaultRepository extends TheRRepository {
  int myIndex;

  public TheRDefaultRepository(String url, int index) {
    super(url);

    myIndex = index;
  }

  public int getIndex() {
    return myIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TheRDefaultRepository that = (TheRDefaultRepository)o;
    return myIndex == that.myIndex;
  }

  @Override
  public int hashCode() {
    return myIndex;
  }
}
