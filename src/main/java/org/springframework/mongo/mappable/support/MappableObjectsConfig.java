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
    private Map<Class<?>, DefaultMappableClassLayout<?>> classLayoutMap =
            new ConcurrentHashMap<Class<?>, DefaultMappableClassLayout<?>>();

    private Class<?> mappableBase;

    public Class<?> getMappableBase() {
        Assert.state(mappableBase != null,
                "Mappable class base should be initialized prior to using mappable functionality");
        return mappableBase;
    }

    public void setMappableBase(Class<?> mappableBase) {
        this.mappableBase = mappableBase;
    }

    public <T> MappableClassLayout<T> getLayout(Class<T> mappableClass) {
        Assert.state(getMappableBase().isAssignableFrom(mappableClass), "Mappable class expected");
        return innerGetLayout(mappableClass);
    }

    private <T> DefaultMappableClassLayout<T> innerGetLayout(Class<T> mappableClass) {
        @SuppressWarnings("unchecked")
        DefaultMappableClassLayout<T> layout = (DefaultMappableClassLayout<T>) classLayoutMap.get(mappableClass);
        if (layout != null) {
            return layout;
        }
        layout = new DefaultMappableClassLayout<T>(mappableClass);
        classLayoutMap.put(mappableClass, layout);
        return layout;
    }

    private final class DefaultMappableClassLayout<T> implements MappableClassLayout<T> {
        private final Class<T> dataObjectClass;
        private String collectionName;
        private List<FieldDescriptor> fieldDescriptors;
        private FieldDescriptor idFieldDescriptor;
        private CursorMapper<?> cursorMapper;

        public DefaultMappableClassLayout(final Class<T> dataObjectClass) {
            this.dataObjectClass = dataObjectClass;
            this.collectionName = dataObjectClass.getSimpleName();

            DefaultMappableClassLayout<? super T> parentLayout = null;
            final Class<? super T> superclass = dataObjectClass.getSuperclass();
            if (getMappableBase().isAssignableFrom(superclass)) {
                parentLayout = innerGetLayout(superclass);
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
        public final DBObject toDBObject(T dataObject) {
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
        @SuppressWarnings("unchecked")
        public CursorMapper<T> getCursorMapper() {
            CursorMapper<?> result = cursorMapper;
            if (result == null) {
                // TODO: analyze from multithreading prospective - OK with double initialization, Not OK with UB
                result = createCursorMapper();
                cursorMapper = result;
            }
            return (CursorMapper<T>) result;
        }

        @Override
        public boolean hasMongoId() {
            return idFieldDescriptor != null;
        }

        @Override
        public Object getMongoId(T object) {
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
