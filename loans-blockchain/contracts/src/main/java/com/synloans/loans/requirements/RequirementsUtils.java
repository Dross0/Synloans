package com.synloans.loans.requirements;


import java.util.Objects;
import java.util.function.Predicate;

public final class RequirementsUtils {
    private static final double EPSILON = 0.000001d;

    private RequirementsUtils(){
        throw new UnsupportedOperationException("Utility class constructor");
    }

    public static void requireThat(boolean expression, String message){
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> void requireThat(Predicate<T> predicate, T object, String message){
        if (!predicate.test(object)){
            throw new IllegalStateException(message);
        }
    }

    public static void requireDoubleEquals(double d1, double d2, double epsilon, String message){
        requireThat(Math.abs(d1 - d2) < epsilon, message);
    }

    public static void requireDoubleEquals(double d1, double d2, String message){
        requireDoubleEquals(d1, d2, EPSILON, message);
    }

    public static void requireNotNull(Object obj, String message){
        requireThat(obj != null, message);
    }

    public static void requireEquals(Object obj1, Object obj2, String message){
        requireThat(Objects.equals(obj1, obj2), message);
    }

    public static void requirePositive(Number number, String message){
        requireThat(number.doubleValue() > 0.0, message);
    }

}
