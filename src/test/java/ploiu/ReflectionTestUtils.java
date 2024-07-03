package ploiu;

public class ReflectionTestUtils {

    // does not work for static or final
    public static void setField(Object object, String field, Object value) {
        try {
            var matchingField = object.getClass().getDeclaredField(field);
            matchingField.setAccessible(true);
            matchingField.set(object, value);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
