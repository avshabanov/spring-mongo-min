package org.springframework.mongo.mappable.support;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.mongo.core.CursorMapper;
import org.springframework.mongo.core.MongoOperations;
import org.springframework.mongo.core.support.MongoTemplate;
import org.springframework.mongo.mappable.MappableMongoOperations;
import org.springframework.mongo.mappable.object.MappableClassLayout;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static org.springframework.mongo.support.MongoUtil.*;

/**
 * Default implementation of the {@link MappableMongoOperations}.
 *
 * @author Alexander Shabanov
 */
public final class MappableMongoTemplate implements MappableMongoOperations {

    private DB db;

    private MongoOperations mo;

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }

    public MongoOperations getMongoOperations() {
        return mo;
    }

    public void setMongoOperations(MongoOperations mongoOperations) {
        this.mo = mongoOperations;
    }

    private MappableObjectsConfig mappableObjectsConfig = new MappableObjectsConfig();

    private boolean initialized = false;
    private boolean constructed = false;

    public MappableMongoTemplate() {
        registerConverters(URI.class,
                new Converter<URI, Object>() {
                    @Override
                    public Object convert(URI source) {
                        return source != null ? source.toString() : null;
                    }
                },
                new Converter<Object, URI>() {
                    @Override
                    public URI convert(Object source) {
                        return source != null ? URI.create(source.toString()) : null;
                    }
                });

        registerConverters(URL.class,
                new Converter<URL, Object>() {
                    @Override
                    public Object convert(URL source) {
                        return source != null ? source.toString() : null;
                    }
                },
                new Converter<Object, URL>() {
                    @Override
                    public URL convert(Object source) {
                        try {
                            return source != null ? new URL(source.toString()) : null;
                        } catch (MalformedURLException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                }
        );
    }

    public MappableMongoTemplate(Class<?> mappableBase) {
        this();
        setMappableBase(mappableBase);
    }

    public MappableMongoTemplate(Class<?> mappableBase, DB db) {
        this(mappableBase);
        setDb(db);
    }

    @PostConstruct
    public void init() {
        Assert.state(initialized, "Mappable base class should be initialized prior to construction");
        constructed = true;

        if (getMongoOperations() == null) {
            // no mongo operations
            if (getDb() != null) {
                setMongoOperations(new MongoTemplate(getDb()));
            } else {
                throw new BeanInitializationException("Neither mongOperations nor db property was set");
            }
        }
    }

    @Override
    public void setMappableBase(Class<?> mappableBase) {
        Assert.state(!constructed, "Mappable base can not be initialized after construction of this instance");
        Assert.notNull(mappableBase, "Mappable base can not be null");
        mappableObjectsConfig.setMappableBase(mappableBase);
        initialized = true;
    }

    @Override
    public <T> void registerConverters(Class<T> clazz, Converter<T, Object> javaToMongo, Converter<Object, T> mongoToJava) {
        Assert.state(!constructed, "Mappable base can not be initialized after construction of this instance");
        mappableObjectsConfig.registerConverters(clazz, javaToMongo, mongoToJava);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String insert(Object object) {
        Assert.notNull(object, "object can not be null");
        final MappableClassLayout classLayout = getLayout(object);
        return mo.insert(classLayout.getCollectionName(), classLayout.toDBObject(object));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(Object object) {
        Assert.notNull(object, "object can not be null");
        final MappableClassLayout classLayout = getLayout(object);
        if (!classLayout.hasMongoId()) {
            throw new IncorrectUpdateSemanticsDataAccessException("It is not possible to update object without inner ID");
        }
        final DBObject query = new BasicDBObject(ID, classLayout.getMongoId(object));
        expectOneUpdate(mo.update(classLayout.getCollectionName(), query, classLayout.toDBObject(object)));
    }

    @Override
    public int remove(Class<?> clazz, String id) {
        return remove(clazz, withId(id));
    }

    @Override
    public int remove(Class<?> clazz, String fieldName, Object value) {
        return remove(clazz, new BasicDBObject(fieldName, value));
    }

    @Override
    public int remove(Class<?> clazz, DBObject query) {
        final MappableClassLayout classLayout = getLayout(clazz);
        return mo.remove(classLayout.getCollectionName(), query).getN();
    }


    @Override
    public <T> T queryById(final Class<T> resultClass, String id) {
        final MappableClassLayout classLayout = getLayout(resultClass);
        @SuppressWarnings("unchecked")
        final CursorMapper<T> cursorMapper = (CursorMapper<T>) classLayout.getCursorMapper();
        return mo.queryForObject(classLayout.getCollectionName(), cursorMapper, withId(id));
    }

    @Override
    public <T> List<T> query(Class<T> resultClass, DBObject query) {
        return query(resultClass, query, null);
    }

    @Override
    public <T> List<T> query(Class<T> resultClass, DBObject query, DBObject orderBy) {
        final MappableClassLayout classLayout = getLayout(resultClass);
        @SuppressWarnings("unchecked")
        final CursorMapper<T> cursorMapper = (CursorMapper<T>) classLayout.getCursorMapper();
        return mo.query(classLayout.getCollectionName(), cursorMapper, query, orderBy);
    }

    @Override
    public <T> List<T> query(Class<T> resultClass, String key, Object value) {
        return query(resultClass, new BasicDBObject(key, value));
    }

    @Override
    public <T> T queryForObject(Class<T> resultClass, String key, Object value) {
        return queryForObject(resultClass, new BasicDBObject(key, value));
    }

    @Override
    public <T> T queryForObject(Class<T> resultClass, DBObject query) {
        final MappableClassLayout classLayout = getLayout(resultClass);
        @SuppressWarnings("unchecked")
        final CursorMapper<T> cursorMapper = (CursorMapper<T>) classLayout.getCursorMapper();
        return mo.queryForObject(classLayout.getCollectionName(), cursorMapper, query);
    }

    @Override
    public <T> MappableClassLayout<T> getLayout(Class<T> mappableClass) {
        Assert.notNull(mappableClass, "Mappable class shall not be null");
        return mappableObjectsConfig.getLayout(mappableClass);
    }

    //
    // Private
    //

    private MappableClassLayout getLayout(Object mappableObject) {
        Assert.notNull(mappableObject, "Mappable object shall not be null");
        return getLayout(mappableObject.getClass());
    }
}
