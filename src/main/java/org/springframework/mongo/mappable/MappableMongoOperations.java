package org.springframework.mongo.mappable;

import org.springframework.mongo.core.MongoOperations;
import org.springframework.mongo.mappable.object.MappableDataObject;

/**
 * Encapsulates basic high-level operations on the {@link org.springframework.mongo.mappable.object.MappableDataObject} which is
 * the simple ORM implementation for mongo.
 *
 * TODO: stabilize
 *
 * @author Alexander Shabanov
 */
public interface MappableMongoOperations {
    MongoOperations getMongoOperations();

    String insert(MappableDataObject object);

    void update(MappableDataObject object);

    <T extends MappableDataObject> T getById(String id, Class<T> resultClass);
}
