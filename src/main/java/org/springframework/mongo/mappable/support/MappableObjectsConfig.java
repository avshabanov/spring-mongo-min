package org.springframework.mongo.mappable.support;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.mongo.core.CursorMapper;
import org.springframework.mongo.mappable.object.MappableClassLayout;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexander Shabanov
 */
final class MappableObjectsConfig {
    private Map<Class<?>, DefaultMappableClassLayout> classLayoutMap =
            new ConcurrentHashMap<Class<?>, DefaultMappableClassLayout>();

    private Class<?> mappableBase;

    public Class<?> getMappableBase() {
        Assert.state(mappableBase != null,
                "Mappable class base should be initialized prior to using mappable functionality");
        return mappableBase;
    }

    public void setMappableBase(Class<?> mappableBase) {
        this.mappableBase = mappableBase;
    }

    public MappableClassLayout getLayout(Class<?> mappableClass) {
        Assert.state(getMappableBase().isAssignableFrom(mappableClass), "Mappable class expected");
        return innerGetLayout(mappableClass);
    }

    private DefaultMappableClassLayout innerGetLayout(Class<?> mappableClass) {
        DefaultMappableClassLayout layout = classLayoutMap.get(mappableClass);
        if (layout != null) {
            return layout;
        }
        layout = new DefaultMappableClassLayout(mappableClass);
        classLayoutMap.put(mappableClass, layout);
        return layout;
    }

    private final class DefaultMappableClassLayout implements MappableClassLayout {
        private final Class<?> dataObjectClass;
        private String collectionName;
        private List<FieldDescriptor> fieldDescriptors;
        private FieldDescriptor idFieldDescriptor;
        private CursorMapper<?> cursorMapper;

        public DefaultMappableClassLayout(final Class<?> dataObjectClass) {
            this.dataObjectClass = dataObjectClass;
            this.collectionName = dataObjectClass.getSimpleName();

            DefaultMappableClassLayout parentLayout = null;
            final Class superclass = dataObjectClass.getSuperclass();
            if (getMappableBase().isAssignableFrom(superclass)) {
                @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
                final Class<Object> castedSuperclass = superclass;
                parentLayout = innerGetLayout(castedSuperclass);
            }

            final List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>();
            if (parentLayout != null) {
                fieldDescriptors.addAll(parentLayout.fieldDescriptors);
            }

            for (final Field field : dataObjectClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                FieldDescriptor fieldDescriptor = new FieldDescriptor(field, MappableObjectsConfig.this);
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
        public final DBObject toDBObject(Object dataObject) {
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
        public CursorMapper<?> getCursorMapper() {
            CursorMapper<?> result = cursorMapper;
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
        public Object getMongoId(Object object) {
            if (idFieldDescriptor == null) {
                throw new IllegalStateException("id field does not exist in this object");
            }

            try {
                final Field field = idFieldDescriptor.getField();
                field.setAccessible(true);
                final Object javaValue = field.get(object);
                return idFieldDescriptor.getJavaToMongoConverter().convert(javaValue);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        private CursorMapper<?> createCursorMapper() {
            return new CursorMapper<Object>() {
                @Override
                public Object mapCursor(DBObject cursor, int rowNum) {
                    try {
                        final Object instance = dataObjectClass.newInstance();
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
}
