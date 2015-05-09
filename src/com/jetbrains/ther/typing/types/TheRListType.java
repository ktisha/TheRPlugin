package com.jetbrains.ther.typing.types;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.psi.api.TheRNumericLiteralExpression;
import com.jetbrains.ther.psi.api.TheRStringLiteralExpression;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TheRListType extends TheRType {

  private static class TypeDescriptor {
    static final TypeDescriptor UNKNOWN = new TypeDescriptor(null, TheRNullType.INSTANCE);
    final String myName;
    final TheRType myType;

    private TypeDescriptor(String name, TheRType type) {
      myName = name;
      myType = type;
    }
  }

  private final Map<String, TheRType> myFields = new HashMap<String, TheRType>();
  private final List<TypeDescriptor> myPositionalTypes = new ArrayList<TypeDescriptor>();
  //shows if we know all fields
  private final boolean myPrecise;

  public TheRListType() {
    this(true);
  }

  public TheRListType(boolean isPrecise) {
    myPrecise = isPrecise;
  }

  public TheRListType(TheRListType other) {
    myFields.putAll(other.myFields);
    myPositionalTypes.addAll(other.myPositionalTypes);
    myPrecise = other.myPrecise;
  }

  @Override
  public String getName() {
    if (myPrecise) {
      return "list(" + StringUtil.join(myPositionalTypes, new Function<TypeDescriptor, String>() {
        @Override
        public String fun(TypeDescriptor descriptor) {
          String typeName = descriptor.myType.getName();
          String name = descriptor.myName;
          return (name == null) ? typeName : name + ": " + typeName;
        }
      }, ", ") + ")";
    } else {
      return "list{" + StringUtil.join(myFields.entrySet(), new Function<Map.Entry<String, TheRType>, String>() {
        @Override
        public String fun(Map.Entry<String, TheRType> entry) {
          return entry.getKey() + ": " + entry.getValue().getName();
        }
      }, ", ") + "}";
    }
  }

  public void addField(String name, int index, TheRType fieldType) {
    if (name != null) {
      myFields.put(name, fieldType);
    }
    if (myPrecise) {
      while (myPositionalTypes.size() <= index) {
        myPositionalTypes.add(TypeDescriptor.UNKNOWN);
      }
      if (index >= 0) {
        myPositionalTypes.set(index, new TypeDescriptor(name, fieldType));
      }
    }
  }

  public void addField(int index, TheRType fieldType) {
    String name = null;
    if (index >= 0 && index < myPositionalTypes.size()) {
      name = myPositionalTypes.get(index).myName;
    }
    addField(name, index, fieldType);
  }

  public void addField(String name, TheRType fieldType) {
    int index = myPositionalTypes.size();
    if (name != null && myPrecise) {
      for (int i = 0; i < myPositionalTypes.size(); i++) {
        if (name.equals(myPositionalTypes.get(i).myName)) {
          index = i;
          break;
        }
      }
    }
    addField(name, index, fieldType);
  }

  public void addField(TheRType fieldType) {
    addField(null, fieldType);
  }

  public void removeField(String name) {
    myFields.remove(name);
    if (name != null && myPrecise) {
      Iterator<TypeDescriptor> it = myPositionalTypes.iterator();
      while (it.hasNext()) {
        TypeDescriptor desc = it.next();
        if (name.equals(desc.myName)) {
          it.remove();
          return;
        }
      }
    }
  }

  public void removeField(int position) {
    if (myPrecise && position >=0 && position < myPositionalTypes.size()) {
      String name = myPositionalTypes.get(position).myName;
      myPositionalTypes.remove(position);
      myFields.remove(name);
    }
  }

  @Nullable
  public TheRType getFieldType(String name) {
    if (myFields.containsKey(name)) {
      return myFields.get(name);
    }
    String partialMatching = null;
    for (String field : myFields.keySet()) {
      if (field.startsWith(name)) {
        if (partialMatching != null) {
          return TheRType.UNKNOWN;
        }
        partialMatching = field;
      }
    }
    if (partialMatching != null) {
      return myFields.get(partialMatching);
    }
    return myPrecise ? TheRNullType.INSTANCE : TheRType.UNKNOWN;
  }


  private TypeDescriptor getFieldDescriptor(int position) {
    if (myPrecise && position >= 0 && position < myPositionalTypes.size()) {
      return myPositionalTypes.get(position);
    }
    return TypeDescriptor.UNKNOWN;
  }

  @Override
  public TheRType getSubscriptionType(List<TheRExpression> indices, boolean isSingleBracket) {
    if (indices.isEmpty()) {
      return this;
    }
    if (indices.size() > 1) {
      return TheRType.UNKNOWN;
    }
    TheRExpression index = indices.get(0);
    TheRType type = TheRType.UNKNOWN;
    String indexName = null;
    if (index instanceof TheRStringLiteralExpression) {
      String quoted = index.getText();
      indexName = quoted.substring(1, quoted.length() - 1);
      type = getFieldType(indexName);
    }
    if (index instanceof TheRNumericLiteralExpression) {
      try {
        int i = Integer.parseInt(index.getText());
        TypeDescriptor descriptor = getFieldDescriptor(i - 1);
        indexName = descriptor.myName;
        type = descriptor.myType;
      } catch (NumberFormatException e) {
        // Do nothing
      }
    }
    if (type != TheRNullType.INSTANCE && type != TheRType.UNKNOWN && isSingleBracket) {
      TheRListType listType = new TheRListType();
      listType.addField(indexName, type);
      type = listType;
    }
    return type;
  }

  // handles assignment to subscription expression
  @Override
  public TheRType afterSubscriptionType(List<TheRExpression> indices, TheRType valueType) {
    if (indices.isEmpty()) {
      return this;
    }
    if (indices.size() > 1) {
      return TheRType.UNKNOWN;
    }
    if (valueType instanceof TheRListType) {
      TheRListType list = (TheRListType)valueType;
      if (!list.myPrecise) {
        return TheRType.UNKNOWN;
      }
      if (list.myPositionalTypes.isEmpty()) {
        return TheRType.UNKNOWN;
      }
      valueType = list.myPositionalTypes.get(0).myType;
    }
    TheRExpression index = indices.get(0);
    TheRListType resultType = new TheRListType(this);
    if (index instanceof TheRStringLiteralExpression) {
      String quoted = index.getText();
      String indexName = quoted.substring(1, quoted.length() - 1);
      if (valueType instanceof TheRNullType) {
        resultType.removeField(indexName);
      } else {
        resultType.addField(indexName, valueType);
      }
    }
    if (index instanceof TheRNumericLiteralExpression) {
      try {
        int i = Integer.parseInt(index.getText());
        if (valueType instanceof TheRNullType) {
          resultType.removeField(i - 1);
        } else {
          resultType.addField(i - 1, valueType);
        }
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
    return resultType;
  }
}
