package org.springframework.mongo.core;

import com.mongodb.DBObject;
import com.mongodb.WriteResult;

import java.util.List;

/**
 * Interface specifying a basic set of JDBC operations.
 * Implemented by {@link org.springframework.mongo.core.support.MongoTemplate}. Not often used directly, but a useful
 * option to enhance testability, as it can easily be mocked or stubbed.
 *
 * TODO: stabilize
 *
 * @author Alexander Shabanov
 * @see org.springframework.mongo.core.support.MongoTemplate
 */
public interface MongoOperations {
    String insert(String collectionName, DBObject dbObject);

    WriteResult update(String collectionName, DBObject query, DBObject dbObject);

    WriteResult remove(String collectionName, DBObject query);

    <T> List<T> query(String collectionName, CursorMapper<T> mapper, String key, Object value);

    <T> List<T> query(String collectionName, CursorMapper<T> mapper, DBObject query);

    <T> List<T> query(String collectionName, CursorMapper<T> mapper, DBObject query, DBObject orderBy);

    <T> List<T> query(String collectionName, String resultFieldName, Class<T> resultClass, DBObject query, DBObject orderBy);

    <T> T queryForObject(String collectionName, CursorMapper<T> mapper, String key, Object value);

    <T> T queryForObject(String collectionName, CursorMapper<T> mapper, DBObject queryObject);

    <T> T queryForObject(String collectionName, String resultFieldName, Class<T> resultClass, DBObject query);
}
