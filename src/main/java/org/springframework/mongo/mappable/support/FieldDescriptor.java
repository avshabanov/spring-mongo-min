package org.springframework.mongo.mappable.support;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.mongo.core.CursorMapper;
import org.springframework.mongo.mappable.object.MappableClassLayout;
import org.springframework.mongo.support.MongoUtil;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
final class FieldDescriptor {
    /**
     * Default name of the mappable objects
     */
    public static final String ID_FIELD = "id";

    private String mongoName;
    private Field field;
    private Converter<Object, Object> mongoToJavaConverter;
    private Converter<Object, Object> javaToMongoConverter;

    public FieldDescriptor(Field field, MappableObjectsConfig config) {
        this.field = field;
        Class fieldType = field.getType();
        if (ID_FIELD.equals(field.getName())) {
            mongoName = MongoUtil.ID; // special case for _id field
            if (fieldType.equals(String.class)) {
                mongoToJavaConverter = OBJECT_ID_STRING;
                javaToMongoConverter = STRING_TO_OBJECT_ID;
            } else {
                throw new IllegalStateException("Field " + field + " is considered as an id field, " +
                        "but it isn't of any known id type");
            }
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            initConvertersForCollectionType(config);
        } else if (config.getMappableBase().isAssignableFrom(fieldType)) {
            initConvertersForMappableType(config);
        } else {
            // TODO: verify types once again
            mongoToJavaConverter = AS_IS;
            javaToMongoConverter = AS_IS;
        }

        Assert.state(mongoToJavaConverter != null && javaToMongoConverter != null);

        // use field name by default
        if (mongoName == null) {
            mongoName = field.getName();
        }
    }

    public boolean isId() {
        return MongoUtil.ID.equals(mongoName);
    }

    public String getMongoName() {
        return mongoName;
    }

    public Field getField() {
        return field;
    }

    public Converter<Object, Object> getMongoToJavaConverter() {
        return mongoToJavaConverter;
    }

    public Converter<Object, Object> getJavaToMongoConverter() {
        return javaToMongoConverter;
    }

    private void initConvertersForMappableType(MappableObjectsConfig config) {
        final Class<?> mappableClass = field.getType();
        mongoToJavaConverter = new MongoMappableObjectConverter(mappableClass, config);
        javaToMongoConverter = new JavaMappableObjectConverter(mappableClass, config);
    }

    private void initConvertersForCollectionType(MappableObjectsConfig config) {
        final Type genericType = field.getGenericType();

        if (!(genericType instanceof ParameterizedType)) {
            throw new IllegalStateException("Can't deal with non-parameterized generic type");
        }

        final ParameterizedType parameterizedType = (ParameterizedType) genericType;
        final Type[] typeArgs = parameterizedType.getActualTypeArguments();
        Assert.state(typeArgs.length == 1);
        final Type argType = typeArgs[0];

        if (!(argType instanceof Class)) {
            throw new IllegalStateException("Parameterized type are expected to specify classes parameters");
        }

        final Class argClass = (Class) argType;
        if (!config.getMappableBase().isAssignableFrom(argClass)) {
            if (String.class.equals(argClass) || Number.class.isAssignableFrom(argClass)) {
                // ok, collection of primitives
                // TODO: test
                mongoToJavaConverter = AS_IS;
                javaToMongoConverter = AS_IS;
            } else {
                throw new IllegalStateException("Unrecognized collection type for field=" + field);
            }
        } else {
            mongoToJavaConverter = new MongoMappableCollectionConverter(argClass, config);
            javaToMongoConverter = new JavaMappableCollectionConverter(argClass, config);
        }
    }

    private static final class MongoMappableObjectConverter implements Converter<Object, Object> {
        private final Class<?> clazz;
        private final MappableObjectsConfig config;

        public MongoMappableObjectConverter(Class<?> clazz, MappableObjectsConfig config) {
            this.clazz = clazz;
            this.config = config;
        }

        @Override
        public Object convert(Object source) {
            if (source == null) {
                return null;
            }

            final MappableClassLayout layout = config.getLayout(clazz);
            final DBObject dbSource = (DBObject) source;
            final CursorMapper<?> mapper = layout.getCursorMapper();
            return mapper.mapCursor(dbSource, 0);
        }
    }

    private static final class JavaMappableObjectConverter implements Converter<Object, Object> {
        private final Class<?> clazz;
        private final MappableObjectsConfig config;

        public JavaMappableObjectConverter(Class<?> clazz, MappableObjectsConfig config) {
            this.clazz = clazz;
            this.config = config;
        }

        @Override
        public Object convert(Object source) {
            if (source == null) {
                return null;
            }

            final MappableClassLayout layout = config.getLayout(clazz);
            return layout.toDBObject(source);
        }
    }

    private static final class MongoMappableCollectionConverter implements Converter<Object, Object> {
        private final Class<?> clazz;
        private final MappableObjectsConfig config;

        public MongoMappableCollectionConverter(Class<?> clazz, MappableObjectsConfig config) {
            this.clazz = clazz;
            this.config = config;
        }

        @Override
        public Object convert(Object source) {
            final MappableClassLayout layout = config.getLayout(clazz);

            @SuppressWarnings("unchecked")
            final Collection<DBObject> dbObjects = (Collection<DBObject>) source;
            final List<Object> result = new ArrayList<Object>(dbObjects.size());
            final CursorMapper<?> mapper = layout.getCursorMapper();
            int row = 0;
            for (final DBObject dbObject : dbObjects) {
                result.add(mapper.mapCursor(dbObject, row++));
            }
            return result;
        }
    }

    private final class JavaMappableCollectionConverter implements Converter<Object, Object> {
        private final Class<?> clazz;
        private final MappableObjectsConfig config;

        public JavaMappableCollectionConverter(Class<?> clazz, MappableObjectsConfig config) {
            this.clazz = clazz;
            this.config = config;
        }

        @Override
        public Object convert(Object source) {
            final MappableClassLayout layout = config.getLayout(clazz);

            final Collection javaObjects = (Collection) source;
            final BasicDBList list = new BasicDBList();
            for (final Object dataObject : javaObjects) {
                list.add(layout.toDBObject(dataObject));
            }
            return list;
        }
    }

    private static final Converter<Object, Object> OBJECT_ID_STRING = new Converter<Object, Object>() {
        @Override
        public Object convert(Object source) {
            return source != null ? ((ObjectId) source).toStringMongod() : null;
        }
    };

    private static final Converter<Object, Object> STRING_TO_OBJECT_ID = new Converter<Object, Object>() {
        @Override
        public Object convert(Object source) {
            return source != null ? new ObjectId(source.toString(), false) : null;
        }
    };

    private static final Converter<Object, Object> AS_IS = new Converter<Object, Object>() {
        @Override
        public Object convert(Object source) {
            return source;
        }
    };
}
