package com.jetbrains.ther.typing.types;

import com.jetbrains.ther.typing.TheRTypeProvider;

import java.util.*;

public class TheRUnionType extends TheRType {
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
    unpackUnions(types);
    types = mergeSimilar(types);

    return new TheRUnionType(types);
  }

  private static Set<TheRType> mergeSimilar(Set<TheRType> types) {
    List<TheRType> typeList = new ArrayList<TheRType>(types);
    Map<TheRType, TheRType> parentTypes = new HashMap<TheRType, TheRType>(types.size());

    //initially each type is itself parent
    for (TheRType type : types) {
      parentTypes.put(type, type);
    }

    //merge like simple DSU
    for (int i = 0; i < typeList.size(); i++) {
      TheRType curType = typeList.get(i);
      for (int j = i + 1; j < typeList.size(); j++) {
        TheRType type = typeList.get(j);
        mergeTypes(parentTypes, curType, type);
      }
    }
    return new HashSet<TheRType>(parentTypes.values());
  }

  private static void mergeTypes(Map<TheRType, TheRType> parentTypes, TheRType curType, TheRType type) {
    TheRType curTypeParent = parentTypes.get(curType);
    TheRType typeParent = parentTypes.get(type);
    if (TheRTypeProvider.isSubtype(curTypeParent, typeParent)) {
      parentTypes.put(curType, typeParent);
      return;
    }
    if (TheRTypeProvider.isSubtype(typeParent, curTypeParent)) {
      parentTypes.put(type, curTypeParent);
    }
  }

  private static void unpackUnions(Set<TheRType> types) {
    for (TheRType type : types) {
      if (type instanceof TheRUnionType) {
        types.addAll(((TheRUnionType)type).myTypes);
      }
    }
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
