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

    void remove(Class<?> clazz, String id);

    <T> T queryById(Class<T> resultClass, String id);

    <T> List<T> query(Class<T> resultClass, DBObject query);

    <T> List<T> query(Class<T> resultClass, DBObject query, DBObject orderBy);

    <T> List<T> query(Class<T> resultClass, String key, Object value);

    <T> T queryForObject(Class<T> resultClass, String key, Object value);

    <T> T queryForObject(Class<T> resultClass, DBObject queryObject);
}
