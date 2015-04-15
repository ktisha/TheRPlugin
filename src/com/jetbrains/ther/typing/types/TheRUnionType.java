package com.jetbrains.ther.typing.types;

import java.util.Set;

public class TheRUnionType extends TheRType{
  private Set<TheRType> myTypes;

  @Override
  public String getName() {
    return "union";
  }

  public TheRUnionType(Set<TheRType> types) {
    myTypes = types;
  }

  public static TheRType create(Set<TheRType> types) {
    if (types.isEmpty()) {
      return TheRType.UNKNOWN;
    }
    if (types.size() == 1) {
      return types.iterator().next();
    }
    return new TheRUnionType(types);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TheRUnionType)) {
      return super.equals(o);
    }
    TheRUnionType type = (TheRUnionType)o;
    if (type.myTypes.size() != myTypes.size()) {
      return false;
    }
    for (TheRType t : myTypes) {
      if (!type.myTypes.contains(t)) {
        return false;
      }
    }
    return true;
  }

  public boolean contains(TheRType type) {
    return myTypes.contains(type);
  }
}
