package com.jetbrains.ther.typing.types;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.Predicate;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.typing.TheRTypeProvider;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TheRS4ClassType extends TheRType {

  private String myName;
  private Map<String, TheRType> mySlots;
  private TheRType mySuperClass;

  public TheRS4ClassType(String name, Map<String, TheRType> slots, TheRType superClass) {
    myName = name;
    mySlots = slots;
    mySuperClass = superClass;
  }

  @Override
  public String getCanonicalName() {
    return "S4(" + myName + ", " + StringUtil.join(mySlots.entrySet(), new Function<Map.Entry<String, TheRType>, String>() {
      @Override
      public String fun(Map.Entry<String, TheRType> entry) {
        return entry.getKey() + ": " + entry.getValue().getName();
      }
    }, ",") + (mySuperClass != null ? " : " + mySuperClass.getName() : "") + ")";
  }

  public TheRType getSuperClass() {
    return mySuperClass;
  }

  public boolean hasSlot(String slot) {
    return mySlots.containsKey(slot);
  }

  public TheRType getSlotType(String slot) {
    if (mySlots.containsKey(slot)) {
      return mySlots.get(slot);
    }
    if (mySuperClass != null && mySuperClass instanceof TheRS4ClassType) {
      return mySuperClass.getSlotType(slot);
    }
    return new TheRErrorType(toString() + " don't have slot " + slot);
  }

  public Collection<String> getSlots() {
    return mySlots.keySet();
  }

  @Nullable
  public static TheRS4ClassType createFromSetClass(TheRCallExpression callExpression) {
    return createFromSetClass(callExpression, new HashSet<String>());
  }

  @Nullable
  private static TheRS4ClassType createFromSetClass(TheRCallExpression callExpression, Set<String> recursionGuard) {
    Map<String, TheRExpression> params = TheRPsiUtils.findParameterValues(callExpression, "Class", "slots", "representation", "contains");

    String name = null;
    Map<String, TheRType> representation = new HashMap<String, TheRType>();
    TheRType superClass = null;

    TheRExpression nameExpression = params.get("Class");
    if (nameExpression != null && nameExpression instanceof TheRStringLiteralExpression) {
      name = nameExpression.getText().substring(1, nameExpression.getText().length() - 1);
    }

    if (name == null) {
      return null;
    }
    recursionGuard.add(name);

    TheRExpression representationExpression = params.get("slots");
    if (representationExpression == null) {
      representationExpression = params.get("representation");
    }
    if (representationExpression != null && representationExpression instanceof TheRCallExpression) {
      TheRCallExpression representationCall = ((TheRCallExpression)representationExpression);
      String functionName = representationCall.getExpression().getName();
      if ("c".equals(functionName) || "representation".equals(functionName)) {
        for (TheRExpression slot : representationCall.getArgumentList().getExpressionList()) {
          if (slot instanceof TheRStringLiteralExpression) {
            String slotName = slot.getText().substring(1, slot.getText().length() - 1);
            representation.put(slotName, TheRUnknownType.INSTANCE);
            continue;
          }
          if (!TheRAssignmentStatement.class.isInstance(slot)) {
            continue;
          }
          TheRAssignmentStatement slotAssignment = ((TheRAssignmentStatement)slot);
          TheRPsiElement assignedValue = slotAssignment.getAssignedValue();
          if (!TheRStringLiteralExpression.class.isInstance(assignedValue)) {
            continue;
          }
          String slotName = slotAssignment.getAssignee().getText();
          String slotTypeName = assignedValue.getText().substring(1, assignedValue.getText().length() - 1);
          TheRType slotType = TheRTypeProvider.findTypeByName(slotTypeName);
          if (slotType == null) {
            slotType = TheRS4ClassType.byName(callExpression.getProject(), slotTypeName);
          }
          if (slotType == null) {
            slotType = TheRUnknownType.INSTANCE;
          }
          representation.put(slotName, slotType);
        }
      }
    }

    TheRExpression superClassExpression = params.get("contains");
    if (superClassExpression != null && superClassExpression instanceof TheRStringLiteralExpression) {
      String superClassName = superClassExpression.getText().substring(1, superClassExpression.getText().length() - 1);
      superClass = TheRTypeProvider.findTypeByName(superClassName);
      if (superClass == null) {
        superClass = TheRS4ClassType.byName(callExpression.getProject(), superClassName);
      }
      if (superClass == null) {
        superClass = TheRUnknownType.INSTANCE;
      }
    }
    return new TheRS4ClassType(name, representation, superClass);
  }

  public static TheRS4ClassType byName(Project project, final String name) {
    return byName(project, name, new HashSet<String>());
  }

  private static TheRS4ClassType byName(Project project, final String name, Set<String> recursionGuard) {
    if (recursionGuard.contains(name)) {
      return null;
    }
    TheRCallExpression call = TheRPsiUtils.findCall(project, "setClass", new Predicate<TheRCallExpression>() {
      @Override
      public boolean apply(@Nullable TheRCallExpression input) {
        TheRExpression s4Class = TheRPsiUtils.findParameterValue("Class", input);
        if (s4Class == null || !TheRStringLiteralExpression.class.isInstance(s4Class)) {
          return false;
        }
        String text = s4Class.getText();
        return name.equals(text.substring(1, text.length() - 1));
      }
    });
    return call != null ? TheRS4ClassType.createFromSetClass(call, recursionGuard) : null;
  }

  @Override
  public TheRS4ClassType clone() {
    TheRS4ClassType s4 = (TheRS4ClassType)super.clone();
    s4.mySlots = new HashMap<String, TheRType>(mySlots);
    return s4;
  }

  @Override
  public TheRType getSubscriptionType(List<TheRExpression> expressions, boolean isSingleBracket) {
    if (mySuperClass == null) {
      return TheRUnknownType.INSTANCE;
    }
    return mySuperClass.getSubscriptionType(expressions, isSingleBracket);
  }

  @Override
  public TheRType afterSubscriptionType(List<TheRExpression> arguments, TheRType valueType) {
    if (mySuperClass == null) {
      return TheRUnknownType.INSTANCE;
    }
    return mySuperClass.afterSubscriptionType(arguments, valueType);
  }

  @Override
  public TheRType getElementTypes() {
    if (mySuperClass != null) {
      return super.getElementTypes();
    }
    return new TheRErrorType("Wrong sequence type");
  }

  @Override
  public TheRType getMemberType(String tag) {
    if (mySuperClass != null) {
      return mySuperClass.getMemberType(tag);
    }
    return new TheRErrorType("$ isn't defined for S4 Classes");
  }

  @Override
  public TheRType afterMemberType(String tag, TheRType valueType) {
    if (mySuperClass == null) {
      return new TheRErrorType("$ isn't defined for S4 class");
    }
    if (mySuperClass instanceof TheRListType) { // R is really works this way :(
      TheRS4ClassType cloned = clone();
      cloned.mySuperClass = mySuperClass.afterMemberType(tag, valueType);
      return cloned;
    }
    return mySuperClass.afterMemberType(tag, valueType);
  }
}
