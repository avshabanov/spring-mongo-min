package org.springframework.mongo;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * Mongo-specific exception that does not map to any other specific data access exception class.
 *
 * @author Alexander Shabanov
 */
public final class UncategorizedMongoException extends UncategorizedDataAccessException {
    /**
     * Constructor for UncategorizedDataAccessException.
     *
     * @param msg   the detail message
     * @param cause the exception thrown by underlying data access API
     */
    public UncategorizedMongoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
