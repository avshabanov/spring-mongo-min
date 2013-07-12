package org.springframework.mongo.support;

import com.mongodb.WriteResult;

/**
 * Represents arbitrary write operation.
 *
 * @author Alexander Shabanov
 */
public interface MongoWriteOperation {
    WriteResult execute();
}
