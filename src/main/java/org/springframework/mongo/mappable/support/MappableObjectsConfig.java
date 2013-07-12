package org.springframework.mongo.mappable.support;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.mongo.core.CursorMapper;
import org.springframework.mongo.mappable.object.MappableClassLayout;
import org.springframework.mongo.mappable.object.MappableDataObject;
import org.springframework.mongo.support.MongoUtil;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexander Shabanov
 */
public final class MappableObjectsConfig {
    /**
     * Default name of the mappable objects
     */
    public static final String ID_FIELD = "id";

    private Map<Class<? extends MappableDataObject>, DefaultMappableClassLayout> classLayoutMap =
            new ConcurrentHashMap<Class<? extends MappableDataObject>, DefaultMappableClassLayout>();

    public MappableClassLayout getLayout(Class<? extends MappableDataObject> dataObjectClass) {
        return innerGetLayout(dataObjectClass);
    }

    private DefaultMappableClassLayout innerGetLayout(Class<? extends MappableDataObject> dataObjectClass) {
        DefaultMappableClassLayout layout = classLayoutMap.get(dataObjectClass);
        if (layout != null) {
            return layout;
        }
        layout = new DefaultMappableClassLayout(dataObjectClass);
        classLayoutMap.put(dataObjectClass, layout);
        return layout;
    }

    private final class DefaultMappableClassLayout implements MappableClassLayout {
        private final Class<? extends MappableDataObject> dataObjectClass;
        private String collectionName;
        private List<FieldDescriptor> fieldDescriptors;
        private FieldDescriptor idFieldDescriptor;
        private CursorMapper<MappableDataObject> cursorMapper;

        public DefaultMappableClassLayout(final Class<? extends MappableDataObject> dataObjectClass) {
            this.dataObjectClass = dataObjectClass;
            this.collectionName = dataObjectClass.getSimpleName();

            DefaultMappableClassLayout parentLayout = null;
            final Class superclass = dataObjectClass.getSuperclass();
            if (MappableDataObject.class.isAssignableFrom(superclass)) {
                @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
                final Class<MappableDataObject> castedSuperclass = superclass;
                parentLayout = innerGetLayout(castedSuperclass);
            }

            final List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>();
            if (parentLayout != null) {
                fieldDescriptors.addAll(parentLayout.fieldDescriptors);
            }

            for (final Field field : dataObjectClass.getDeclaredFields()) {
                FieldDescriptor fieldDescriptor = new FieldDescriptor(field);
                fieldDescriptors.add(fieldDescriptor);
                if (fieldDescriptor.isId()) {
                    if (this.idFieldDescriptor != null) {
                        throw new IllegalStateException("Duplicate ID field in class " + dataObjectClass);
                    }
                    this.idFieldDescriptor = fieldDescriptor;
                }
            }
            this.fieldDescriptors = fieldDescriptors;
        }

        @Override
        public final String getCollectionName() {
            return collectionName;
        }

        @Override
        public final DBObject toDBObject(MappableDataObject dataObject) {
            Assert.state(dataObject.getClass().equals(dataObjectClass), "Class mismatch");
            final BasicDBObject dbObject = new BasicDBObject();
            try {
                for (final FieldDescriptor fieldDescriptor : fieldDescriptors) {
                    if (fieldDescriptor.isId()) {
                        continue;
                    }

                    final Field field = fieldDescriptor.getField();
                    field.setAccessible(true);
                    final Object javaValue = field.get(dataObject);
                    final Object mongoValue = fieldDescriptor.getJavaToMongoConverter().convert(javaValue);
                    dbObject.append(fieldDescriptor.getMongoName(), mongoValue);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            return dbObject;
        }

        @Override
        public CursorMapper<MappableDataObject> getCursorMapper() {
            CursorMapper<MappableDataObject> result = cursorMapper;
            if (result == null) {
                // TODO: analyze from multithreading prospective - OK with double initialization, Not OK with UB
                result = createCursorMapper();
                cursorMapper = result;
            }
            return result;
        }

        @Override
        public boolean hasMongoId() {
            return idFieldDescriptor != null;
        }

        @Override
        public Object getMongoId(MappableDataObject object) {
            Assert.state(idFieldDescriptor != null);
            try {
                final Field field = idFieldDescriptor.getField();
                field.setAccessible(true);
                final Object javaValue = field.get(object);
                return idFieldDescriptor.getJavaToMongoConverter().convert(javaValue);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        private CursorMapper<MappableDataObject> createCursorMapper() {
            return new CursorMapper<MappableDataObject>() {
                @Override
                public MappableDataObject mapCursor(DBObject cursor, int rowNum) {
                    try {
                        final MappableDataObject instance = dataObjectClass.newInstance();
                        for (final FieldDescriptor fieldDescriptor : fieldDescriptors) {
                            final Object mongoValue = cursor.get(fieldDescriptor.getMongoName());
                            final Object javaValue = fieldDescriptor.getMongoToJavaConverter().convert(mongoValue);
                            final Field field = fieldDescriptor.getField();
                            field.setAccessible(true);
                            field.set(instance, javaValue);
                        }
                        return instance;
                    } catch (InstantiationException e) {
                        throw new IllegalStateException(e);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
            };
        }
    }

    private static final class FieldDescriptor {
        private String mongoName;
        private Field field;
        private Converter<Object, Object> mongoToJavaConverter;
        private Converter<Object, Object> javaToMongoConverter;

        public FieldDescriptor(Field field) {
            this.field = field;
            if (ID_FIELD.equals(field.getName())) {
                mongoName = MongoUtil.ID; // special case for _id field
                if (String.class.equals(field.getType())) {
                    mongoToJavaConverter = OBJECT_ID_STRING;
                    javaToMongoConverter = STRING_TO_OBJECT_ID;
                } else {
                    throw new IllegalStateException("Field " + field + " is considered as an id field, " +
                            "but it isn't of any known id type");
                }
            } else {
                mongoName = field.getName();
                mongoToJavaConverter = AS_IS;
                javaToMongoConverter = AS_IS;
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
    }


    private static final Converter<Object, Object> OBJECT_ID_STRING = new Converter<Object, Object>() {
        @Override
        public Object convert(Object source) {
            return ((ObjectId) source).toStringMongod();
        }
    };

    private static final Converter<Object, Object> STRING_TO_OBJECT_ID = new Converter<Object, Object>() {
        @Override
        public Object convert(Object source) {
            return new ObjectId(source.toString(), false);
        }
    };

    private static final Converter<Object, Object> AS_IS = new Converter<Object, Object>() {
        @Override
        public Object convert(Object source) {
            return source;
        }
    };
}
