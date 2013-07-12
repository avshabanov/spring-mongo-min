package org.springframework.mongo.mappable;

import com.mongodb.DBObject;
import org.springframework.mongo.mappable.object.MappableDataObject;

import java.util.List;

/**
 * Encapsulates basic high-level operations on the {@link org.springframework.mongo.mappable.object.MappableDataObject} which is
 * the simple ORM implementation for mongo.
 *
 * TODO: stabilize
 *
 * @author Alexander Shabanov
 */
public interface MappableMongoOperations {
    String insert(MappableDataObject object);

    void update(MappableDataObject object);

    void remove(Class<? extends MappableDataObject> clazz, String id);

    <T extends MappableDataObject> T queryById(Class<T> resultClass, String id);

    <T extends MappableDataObject> List<T> query(Class<T> resultClass, DBObject query);

    <T extends MappableDataObject> List<T> query(Class<T> resultClass, DBObject query, DBObject orderBy);
}
