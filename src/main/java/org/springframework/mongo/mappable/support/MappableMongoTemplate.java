package org.springframework.mongo.mappable.support;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mongo.core.CursorMapper;
import org.springframework.mongo.core.MongoOperations;
import org.springframework.mongo.mappable.MappableMongoOperations;
import org.springframework.mongo.mappable.object.MappableClassLayout;
import org.springframework.mongo.mappable.object.MappableDataObject;
import org.springframework.util.Assert;

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
    public MongoOperations getMongoOperations() {
        return mo;
    }

    @Override
    public String insert(MappableDataObject object) {
        Assert.notNull(object, "Object can not be null");
        final MappableClassLayout classLayout = layoutOf(object);
        return mo.insert(classLayout.getCollectionName(), classLayout.toDBObject(object));
    }

    @Override
    public void update(MappableDataObject object) {
        final MappableClassLayout classLayout = layoutOf(object);
        final DBObject query = new BasicDBObject(ID, classLayout.getMongoId(object));
        expectOneUpdate(mo.update(classLayout.getCollectionName(), query, classLayout.toDBObject(object)));
    }

    @Override
    public <T extends MappableDataObject> T getById(String id, final Class<T> resultClass) {
        final MappableClassLayout classLayout = layoutOf(resultClass);
        @SuppressWarnings("unchecked")
        final CursorMapper<T> cursorMapper = (CursorMapper<T>) classLayout.getCursorMapper();
        return mo.queryForObject(classLayout.getCollectionName(), cursorMapper, withId(id));
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
