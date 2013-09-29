package org.springframework.mongo.mappable;

import com.mongodb.DBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.mongo.mappable.object.MappableClassLayout;

import java.util.List;

/**
 * Encapsulates basic high-level operations on the mappable objects.
 * Consider this as a simple ORM implementation for mongo.
 *
 * TODO: stabilize
 *
 * @author Alexander Shabanov
 */
public interface MappableMongoOperations {

    void setMappableBase(Class<?> mappableBase);

    <T> MappableClassLayout<T> getLayout(Class<T> mappableClass);

    <T> void registerConverters(Class<T> clazz, Converter<T, Object> javaToMongo, Converter<Object, T> mongoToJava);

    //
    // ORM-specific methods
    //

    String insert(Object object);

    void update(Object object);

    int remove(Class<?> clazz, String id);

    int remove(Class<?> clazz, String fieldName, Object value);

    int remove(Class<?> clazz, DBObject object);

    <T> T queryById(Class<T> resultClass, String id);

    <T> List<T> query(Class<T> resultClass, DBObject query);

    <T> List<T> query(Class<T> resultClass, DBObject query, DBObject orderBy);
}
