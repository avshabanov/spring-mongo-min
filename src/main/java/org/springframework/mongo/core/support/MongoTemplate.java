package org.springframework.mongo.core.support;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.mongo.core.CursorMapper;
import org.springframework.mongo.core.MongoOperations;
import org.springframework.mongo.support.MongoWriteOperation;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.mongo.core.support.BuiltinMappers.getMapperFor;
import static org.springframework.mongo.support.MongoUtil.executeWriteOperation;
import static org.springframework.mongo.support.MongoUtil.extractId;

/**
 * <b>This is the central class in the mongo core package.</b>
 *
 * It simplifies the use of mongo and helps to avoid common errors.
 * It executes core mongo workflow, leaving application code to provide mongo queries
 * and extract results. This class executes mongo queries or updates, initiating
 * iteration over cursor objects and catching mongo exceptions and return codes and translating
 * them to the generic, more informative exception hierarchy defined in the
 * {@code org.springframework.dao} package.
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

    @Override
    public <T> List<T> query(String collectionName, CursorMapper<T> mapper, DBObject query) {
        return query(collectionName, mapper, query, null);
    }

    @Override
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

    @Override
    public <T> T queryForObject(String collectionName, String resultFieldName, Class<T> resultClass, DBObject query) {
        return queryForObject(collectionName, getMapperFor(resultFieldName, resultClass), query);
    }

    @Override
    public <T> T queryForObject(String collectionName, CursorMapper<T> mapper, DBObject queryObject) {
        return DataAccessUtils.requiredSingleResult(query(collectionName, mapper, queryObject));
    }

    @Override
    public <T> List<T> query(String collectionName, String resultFieldName, Class<T> resultClass,
                             DBObject query, DBObject orderBy) {
        return query(collectionName, getMapperFor(resultFieldName, resultClass), query, orderBy);
    }
}
