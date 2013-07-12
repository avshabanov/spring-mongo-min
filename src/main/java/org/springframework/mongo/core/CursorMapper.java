package org.springframework.mongo.core;

import com.mongodb.DBObject;

/**
 * An interface used by {@link org.springframework.mongo.core.support.MongoTemplate}
 * for mapping rows of a {@link DBObject} cursor on a per-row basis. Implementations of this
 * interface perform the actual work of mapping present cursor to a result object,
 * but don't need to worry about exception handling.
 * {@link com.mongodb.MongoException} will be caught and handled by the calling MongoTemplate.
 *
 * <p>Typically used either for {@link org.springframework.mongo.core.support.MongoTemplate}'s query methods
 * or for out parameters of stored procedures. Cursor objects are
 * typically stateless and thus reusable; they are an ideal choice for
 * implementing row-mapping logic in a single place.
 *
 * @param <T> Type of the object cursor should be mapped to.
 * @author Alexander Shabanov
 */
public interface CursorMapper<T> {

    /**
     * Implementations must implement this method to map each row of data
     * in the cursor object. This method should not cast the provided cursor
     * object to {@link com.mongodb.DBCursor} to invoke any cursor related functionality then;
     * it is only supposed to map values of the current row.
     *
     * @param cursor the Cursor to map (pre-initialized for the current row)
     * @param rowNum the number of the current row
     * @return the result object for the current row
     */
    T mapCursor(DBObject cursor, int rowNum);
}
