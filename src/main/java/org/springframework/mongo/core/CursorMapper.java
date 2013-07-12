package org.springframework.mongo.core;

import com.mongodb.DBObject;

/**
 * Common interface used to map mongo cursor to the user-defined domain object.
 *
 * @param <T> Type of the object cursor should be mapped to.
 * @author Alexander Shabanov
 */
public interface CursorMapper<T> {
    T mapCursor(DBObject cursor, int rowNum);
}
