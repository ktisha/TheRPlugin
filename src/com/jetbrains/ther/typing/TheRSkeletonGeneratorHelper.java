package com.jetbrains.ther.typing;

import com.jetbrains.ther.typing.types.*;

import java.util.HashMap;
import java.util.Map;

public class TheRSkeletonGeneratorHelper {
  public static final Map<String, TheRType> TYPES = new HashMap<String, TheRType>();
  static {
    TYPES.put("logical", TheRLogicalType.INSTANCE);
    TYPES.put("complex", TheRComplexType.INSTANCE);
    TYPES.put("numeric", TheRNumericType.INSTANCE);
    TYPES.put("integer", TheRIntegerType.INSTANCE);
    TYPES.put("number", TheRComplexType.INSTANCE);
    TYPES.put("raw", TheRRawType.INSTANCE);
    TYPES.put("string", TheRCharacterType.INSTANCE);
    TYPES.put("character", TheRCharacterType.INSTANCE);
    TYPES.put("name", TheRCharacterType.INSTANCE);
  }
}
