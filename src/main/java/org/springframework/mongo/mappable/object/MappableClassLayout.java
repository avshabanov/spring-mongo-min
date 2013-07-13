package org.springframework.mongo.mappable.object;

import com.mongodb.DBObject;
import org.springframework.mongo.core.CursorMapper;

/**
 * Internal representation of the class layout
 *
 * @author Alexander Shabanov
 */
public interface MappableClassLayout {
    String getCollectionName();

    DBObject toDBObject(Object dataObject);

    CursorMapper<?> getCursorMapper();

    Object getMongoId(Object object);

    boolean hasMongoId();
}
