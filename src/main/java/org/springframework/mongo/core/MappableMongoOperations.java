package org.springframework.mongo.core;

import org.springframework.mongo.object.MappableDataObject;

/**
 * Encapsulates basic high-level operations on the {@link MappableDataObject} which is
 * the simple ORM implementation for mongo.
 * TODO: stabilize
 *
 * @author Alexander Shabanov
 */
public interface MappableMongoOperations {
    static final String ID_FIELD = "id";

    MongoOperations getMongoOperations();

    String insert(MappableDataObject object);

    void update(MappableDataObject object);

    <T extends MappableDataObject> T getById(String id, Class<T> resultClass);
}
