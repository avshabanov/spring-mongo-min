package org.springframework.mongo.mappable.object;

import com.mongodb.DBObject;
import org.springframework.mongo.core.CursorMapper;

/**
 * Internal representation of the class layout
 *
 * @author Alexander Shabanov
 */
public interface MappableClassLayout<T> {
    String getCollectionName();

    DBObject toDBObject(T object);

    CursorMapper<T> getCursorMapper();

    Object getMongoId(T object);

    boolean hasMongoId();
}
