package com.jetbrains.ther.typing.types;

import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.typing.TheRTypeProvider;

import java.util.*;

public class TheRUnionType extends TheRType {
  private Set<TheRType> myTypes;

  public Set<TheRType> getTypes() {
    return myTypes;
  }

  @Override
  public String getCanonicalName() {
    return "union";
  }

  private TheRUnionType(Set<TheRType> types) {
    myTypes = types;
  }

  public static TheRType create(Set<TheRType> types) {
    if (types.isEmpty()) {
      return TheRUnknownType.INSTANCE;
    }
    unpackUnions(types  );
    types = mergeSimilar(types);
    if (types.size() == 1) {
      return types.iterator().next();
    }

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
    Set<TheRType> savedTypes =  new HashSet<TheRType>(types);
    types.clear();
    for (TheRType type : savedTypes) {
      if (type instanceof TheRUnionType) {
        types.addAll(((TheRUnionType)type).myTypes);
      } else {
          types.add(type);
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (TheRType type : myTypes) {
      builder.append(type.toString()).append("|");
    }
    builder.deleteCharAt(builder.length() - 1);
    return builder.toString();
  }

  @Override
  public TheRType getSubscriptionType(List<TheRExpression> expressions, boolean isSingleBracket) {
    HashSet<TheRType> subscriptTypes = new HashSet<TheRType>();
    for (TheRType type : myTypes) {
      subscriptTypes.add(type.getSubscriptionType(expressions, isSingleBracket));
    }
    return TheRUnionType.create(subscriptTypes);
  }

  @Override
  public TheRType afterSubscriptionType(List<TheRExpression> arguments, TheRType valueType) {
    HashSet<TheRType> afterTypes = new HashSet<TheRType>();
    for (TheRType type : myTypes) {
      afterTypes.add(type.afterSubscriptionType(arguments, valueType));
    }
    return TheRUnionType.create(afterTypes);
  }

  @Override
  public TheRType getElementTypes() {
    HashSet<TheRType> elementTypes = new HashSet<TheRType>();
    for (TheRType type : myTypes) {
      elementTypes.add(type.getElementTypes());
    }
    return TheRUnionType.create(elementTypes);
  }

  @Override
  public TheRUnionType clone() {
    TheRUnionType result = (TheRUnionType)super.clone();
    result.myTypes = new HashSet<TheRType>(myTypes);
    return result;
  }
}
