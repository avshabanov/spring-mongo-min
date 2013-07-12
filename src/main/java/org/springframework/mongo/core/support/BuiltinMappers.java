package org.springframework.mongo.core.support;

import com.mongodb.DBObject;
import org.springframework.mongo.core.CursorMapper;

/**
 * Utility class that contains builtin mappers for some standard classes such as String and Number descendants.
 *
 * @author Alexander Shabanov
 */
public final class BuiltinMappers {

    @SuppressWarnings("unchecked")
    public static <T> CursorMapper<T> getMapperFor(final String fieldName, Class<T> valueClass) {
        if (Number.class.equals(valueClass)) {
            return (CursorMapper<T>) new CursorMapper<Number>() {
                @Override
                public Number mapCursor(DBObject cursor, int rowNum) {
                    return (Number) cursor.get(fieldName);
                }
            };
        } else if (String.class.equals(valueClass)) {
            return (CursorMapper<T>) new CursorMapper<String>() {
                @Override
                public String mapCursor(DBObject cursor, int rowNum) {
                    return (String) cursor.get(fieldName);
                }
            };
        }

        throw new IllegalArgumentException("No default mapper for class " + valueClass);
    }

    private BuiltinMappers() {}
}
