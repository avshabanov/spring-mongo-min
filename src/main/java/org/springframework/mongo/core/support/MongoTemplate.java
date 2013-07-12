package org.springframework.mongo.core.support;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.mongo.core.CursorMapper;
import org.springframework.mongo.core.MongoOperations;
import org.springframework.mongo.support.MongoWriteOperation;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.mongo.support.MongoUtil.ID;
import static org.springframework.mongo.support.MongoUtil.executeWriteOperation;

/**
 * TODO: comment
 *
 * @author Alexander Shabanov
 */
public class MongoTemplate implements MongoOperations {
    @Autowired
    private DB db;

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }

    @PostConstruct
    public void init() {
        if (db == null) {
            throw new IllegalStateException("DB is not initialized");
        }
    }

    public static BasicDBObject withId(String id) {
        return new BasicDBObject()
                .append(ID, new ObjectId(id, false));
    }

    public static String extractId(DBObject dbObject) {
        final Object idObject = dbObject.get(ID);
        if (idObject == null) {
            throw new DataIntegrityViolationException("No ID field in dbObject=" + dbObject);
        } else if (idObject instanceof ObjectId) {
            return ((ObjectId) idObject).toStringMongod();
        }

        throw new DataIntegrityViolationException("Unrecognized ID field in dbObject=" + dbObject);
    }

    @Override
    public String insert(final String collectionName, final DBObject dbObject) {
        executeWriteOperation(new MongoWriteOperation() {
            @Override
            public WriteResult execute() {
                return getDb().getCollection(collectionName).insert(dbObject);  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        return extractId(dbObject);
    }

    @Override
    public WriteResult update(final String collectionName, final DBObject query, final DBObject dbObject) {
        return executeWriteOperation(new MongoWriteOperation() {
            @Override
            public WriteResult execute() {
                return getDb().getCollection(collectionName).update(query, dbObject);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> CursorMapper<T> getMapperFor(final String fieldName, Class<T> valueClass) {
        if (Long.class.equals(valueClass)) {
            return (CursorMapper<T>) new CursorMapper<Long>() {
                @Override
                public Long mapCursor(DBObject cursor, int rowNum) {
                    return (Long) cursor.get(fieldName);
                }
            };
        }

        throw new IllegalArgumentException("No default mapper for class " + valueClass);
    }

    @Override
    public <T> T queryForObject(String collectionName, CursorMapper<T> mapper, DBObject queryObject) {
        return DataAccessUtils.requiredSingleResult(query(collectionName, mapper, queryObject));
    }

    @Override
    public <T> T queryForObject(String collectionName, CursorMapper<T> mapper, String key, Object value) {
        return queryForObject(collectionName, mapper, new BasicDBObject(key, value));
    }

    public <T> List<T> query(String collectionName, CursorMapper<T> mapper, String key, Object value) {
        return query(collectionName, mapper, new BasicDBObject(key, value));
    }

    public <T> List<T> query(String collectionName, CursorMapper<T> mapper, DBObject query) {
        return query(collectionName, mapper, query, null);
    }

    public <T> List<T> query(String collectionName, CursorMapper<T> mapper, DBObject query, DBObject orderBy) {
        final DBCursor cursor = getDb().getCollection(collectionName).find(query);
        if (orderBy != null) {
            cursor.sort(orderBy);
        }
        final List<T> result = new ArrayList<T>();
        int rowNum = 0;
        while (cursor.hasNext()) {
            cursor.next();
            result.add(mapper.mapCursor(cursor.curr(), rowNum++));
        }
        return Collections.unmodifiableList(result);
    }

    // Class<T> version of the query methods

    public <T> T queryForObject(String collectionName, String resultFieldName, Class<T> resultClass, DBObject query) {
        return queryForObject(collectionName, getMapperFor(resultFieldName, resultClass), query);
    }

    public <T> List<T> query(String collectionName, String resultFieldName, Class<T> resultClass,
                             DBObject query, DBObject orderBy) {
        return query(collectionName, getMapperFor(resultFieldName, resultClass), query, orderBy);
    }
}
