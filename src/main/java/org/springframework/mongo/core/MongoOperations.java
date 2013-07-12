package org.springframework.mongo.core;

import com.mongodb.DBObject;
import com.mongodb.WriteResult;

/**
 * Spring mongo support
 *
 * @author Alexander Shabanov
 */
public interface MongoOperations {
    String insert(String collectionName, DBObject dbObject);

    WriteResult update(String collectionName, DBObject query, DBObject dbObject);

    <T> T queryForObject(String collectionName, CursorMapper<T> mapper, DBObject queryObject);

    <T> T queryForObject(String collectionName, CursorMapper<T> mapper, String key, Object value);
}
