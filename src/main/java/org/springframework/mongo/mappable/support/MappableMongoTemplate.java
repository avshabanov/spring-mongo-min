package org.springframework.mongo.mappable.support;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.mongo.core.CursorMapper;
import org.springframework.mongo.core.MongoOperations;
import org.springframework.mongo.mappable.MappableMongoOperations;
import org.springframework.mongo.mappable.object.MappableClassLayout;
import org.springframework.mongo.mappable.object.MappableDataObject;
import org.springframework.util.Assert;

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

    @Override
    public String insert(MappableDataObject object) {
        Assert.notNull(object, "Object can not be null");
        final MappableClassLayout classLayout = layoutOf(object);
        return mo.insert(classLayout.getCollectionName(), classLayout.toDBObject(object));
    }

    @Override
    public void update(MappableDataObject object) {
        final MappableClassLayout classLayout = layoutOf(object);
        if (!classLayout.hasMongoId()) {
            throw new IncorrectUpdateSemanticsDataAccessException("It is not possible to update object without inner ID");
        }
        final DBObject query = new BasicDBObject(ID, classLayout.getMongoId(object));
        expectOneUpdate(mo.update(classLayout.getCollectionName(), query, classLayout.toDBObject(object)));
    }

    @Override
    public void remove(Class<? extends MappableDataObject> clazz, String id) {
        final MappableClassLayout classLayout = layoutOf(clazz);
        mo.remove(classLayout.getCollectionName(), withId(id));
    }

    @Override
    public <T extends MappableDataObject> T queryById(final Class<T> resultClass, String id) {
        final MappableClassLayout classLayout = layoutOf(resultClass);
        @SuppressWarnings("unchecked")
        final CursorMapper<T> cursorMapper = (CursorMapper<T>) classLayout.getCursorMapper();
        return mo.queryForObject(classLayout.getCollectionName(), cursorMapper, withId(id));
    }

    @Override
    public <T extends MappableDataObject> List<T> query(Class<T> resultClass, DBObject query) {
        return query(resultClass, query, null);
    }

    @Override
    public <T extends MappableDataObject> List<T> query(Class<T> resultClass, DBObject query, DBObject orderBy) {
        final MappableClassLayout classLayout = layoutOf(resultClass);
        @SuppressWarnings("unchecked")
        final CursorMapper<T> cursorMapper = (CursorMapper<T>) classLayout.getCursorMapper();
        return mo.query(classLayout.getCollectionName(), cursorMapper, query, orderBy);
    }

    //
    // Private
    //

    private MappableClassLayout layoutOf(Class<? extends MappableDataObject> dataObjectClass) {
        Assert.notNull(dataObjectClass, "Data object shall not be null");
        return mappableObjectsConfig.getLayout(dataObjectClass);
    }

    private MappableClassLayout layoutOf(MappableDataObject dataObject) {
        Assert.notNull(dataObject, "Data object shall not be null");
        return layoutOf(dataObject.getClass());
    }
}
