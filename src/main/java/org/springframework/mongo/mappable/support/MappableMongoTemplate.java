package org.springframework.mongo.mappable.support;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.mongo.core.CursorMapper;
import org.springframework.mongo.core.MongoOperations;
import org.springframework.mongo.mappable.MappableMongoOperations;
import org.springframework.mongo.mappable.object.MappableClassLayout;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.springframework.mongo.support.MongoUtil.ID;
import static org.springframework.mongo.support.MongoUtil.expectOneUpdate;
import static org.springframework.mongo.support.MongoUtil.withId;

/**
 * Default implementation of the {@link MappableMongoOperations}.
 *
 * @author Alexander Shabanov
 */
public final class MappableMongoTemplate implements MappableMongoOperations {
    @Autowired
    private MongoOperations mo;

    private MappableObjectsConfig mappableObjectsConfig = new MappableObjectsConfig();

    private boolean initialized = false;
    private boolean constructed = false;

    public MappableMongoTemplate() {
    }

    public MappableMongoTemplate(Class<?> mappableBase) {
        this();
        setMappableBase(mappableBase);
    }

    @Override
    public void setMappableBase(Class<?> mappableBase) {
        Assert.state(!constructed, "Mappable base can not be initialized after construction of this instance");
        Assert.notNull(mappableBase, "Mappable base can not be null");
        mappableObjectsConfig.setMappableBase(mappableBase);
        initialized = true;
    }

    @PostConstruct
    public void init() {
        Assert.state(initialized, "Mappable base class should be initialized prior to construction");
        constructed = true;
    }

    @Override
    public String insert(Object object) {
        Assert.notNull(object, "Object can not be null");
        final MappableClassLayout classLayout = getLayout(object);
        return mo.insert(classLayout.getCollectionName(), classLayout.toDBObject(object));
    }

    @Override
    public void update(Object object) {
        final MappableClassLayout classLayout = getLayout(object);
        if (!classLayout.hasMongoId()) {
            throw new IncorrectUpdateSemanticsDataAccessException("It is not possible to update object without inner ID");
        }
        final DBObject query = new BasicDBObject(ID, classLayout.getMongoId(object));
        expectOneUpdate(mo.update(classLayout.getCollectionName(), query, classLayout.toDBObject(object)));
    }

    @Override
    public void remove(Class<?> clazz, String id) {
        final MappableClassLayout classLayout = getLayout(clazz);
        mo.remove(classLayout.getCollectionName(), withId(id));
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
    public MappableClassLayout getLayout(Class<?> mappableClass) {
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
