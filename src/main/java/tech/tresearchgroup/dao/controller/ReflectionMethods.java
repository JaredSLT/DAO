package tech.tresearchgroup.dao.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ReflectionMethods {
    public static Object getNewInstance(Class theClass) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor[] constructors = theClass.getConstructors();
        for (Constructor constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            }
        }
        return null;
    }

    public static Method getId(Class theClass) {
        try {
            return theClass.getMethod("getId");
        } catch (NoSuchMethodException e) {
            System.out.println("Failed to execute: getId on: " + theClass.getSimpleName());
        }
        return null;
    }

    public static Method getGetter(Field field, Class theClass) {
        String cap = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        try {
            if (boolean.class.equals(field.getType())) {
                return theClass.getMethod("is" + cap);
            }
            return theClass.getMethod("get" + cap);
        } catch (NoSuchMethodException e) {
            System.out.println("Failed to execute: get" + cap + " on: " + theClass.getSimpleName());
        }
        return null;
    }

    public static Method getSetter(Field field, Class theClass, Class parameterClass) {
        String cap = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        try {
            return theClass.getMethod("set" + cap, parameterClass);
        } catch (NoSuchMethodException e) {
            System.out.println("Failed to execute: set" + cap + " on: " + theClass.getSimpleName());
        }
        return null;
    }

    public static Object getValueOf(Class theClass, String data) throws InvocationTargetException, IllegalAccessException {
        try {
            if (Objects.equals(data, null)) {
                return null;
            }
            return theClass.getMethod("valueOf", String.class).invoke(theClass, data);
        } catch (NoSuchMethodException e) {
            System.out.println("Failed to execute: valueOf on: " + theClass.getSimpleName());
        }
        return null;
    }

    public static Method setId(Class theClass, Class parameterClass) {
        try {
            return theClass.getMethod("setId", parameterClass);
        } catch (NoSuchMethodException e) {
            System.out.println("Failed to execute: setId on: " + theClass.getSimpleName());
        }
        return null;
    }

    public static boolean isNotArray(Class theClass) {
        return !theClass.isInterface() && !theClass.equals(List.class);
    }

    public static boolean isObject(Class theClass) {
        return !Date.class.equals(theClass) &&
            !Long.class.equals(theClass) &&
            !Integer.class.equals(theClass) &&
            !String.class.equals(theClass) &&
            !Float.class.equals(theClass) &&
            !Byte.class.equals(theClass) &&
            !Character.class.equals(theClass) &&
            !Double.class.equals(theClass) &&
            !long.class.equals(theClass) &&
            !int.class.equals(theClass) &&
            !float.class.equals(theClass) &&
            !byte.class.equals(theClass) &&
            !char.class.equals(theClass) &&
            !boolean.class.equals(theClass) &&
            !double.class.equals(theClass) &&
            !theClass.isEnum() &&
            !theClass.isArray() &&
            !theClass.isInterface();
    }
}
