spring-mongo-min
================

Minimalistic mongo support for Spring-driven projects.

*Note*, that at the moment this is experimental project and its functionality will grow as time goes by.

## Motivation

To provide basic capabilities to easily create mongo-driven DAOs.
spring-jdbc was an inspiration to create similar library.

This module provides only a very basic set of mongo operations so if you feel you need more extensive support you
may consider spring-data-mongo library for that purpose.

## Short summary

Interface to support class:

```java
public interface MongoOperations {
    String insert(String collectionName, DBObject dbObject);

    WriteResult update(String collectionName, DBObject query, DBObject dbObject);

    WriteResult remove(String collectionName, DBObject query);

    <T> List<T> query(String collectionName, CursorMapper<T> mapper, DBObject query);

    <T> List<T> query(String collectionName, CursorMapper<T> mapper, DBObject query, DBObject orderBy);

    <T> List<T> query(String collectionName, String resultFieldName, Class<T> resultClass, DBObject query, DBObject orderBy);

    <T> T queryForObject(String collectionName, CursorMapper<T> mapper, DBObject queryObject);

    <T> T queryForObject(String collectionName, String resultFieldName, Class<T> resultClass, DBObject query);
//...
}
```

Sample usage in the user code:

```java
Profile profile = new Profile("bob", 36);

final String id = mo.insert("Profile", toDBObject(profile));

profile = new Profile(id, profile);
assertEquals(profile, mo.queryForObject("Profile", new ProfileMapper(), withId(id)));

profile = new Profile("dave", 47);
mo.update("Profile", withId(id), toDBObject(profile));

assertEquals(profile, mo.queryForObject("Profile", new ProfileMapper(), withId(id)));

// ...
DBObject toDBObject(Profile value) {
    return new BasicDBObject()
            .append("name", value.getName())
            .append("age", value.getAge());
}

class ProfileMapper implements CursorMapper<Profile> {
    @Override
    public Profile mapCursor(DBObject cursor, int rowNum) {
        return new Profile(extractId(cursor), (String) cursor.get("name"), (Integer) cursor.get("age"));
    }
}
```

```java
Profile profile = new Profile("bob", 36);
final String id = mo.insert("Profile", toDBObject(profile));
assertEquals(ImmutableList.of(new Profile(id, profile)),
        mo.query("Profile", new ProfileMapper(), new BasicDBObject()));

mo.remove("Profile", withId(id));
assertEquals(ImmutableList.<Profile>of(), mo.query("Profile", new ProfileMapper(), new BasicDBObject()));
```

The library also has minimalistic ORM which relies on the reflection and some considerations about the base classes,
like having 'id' field in all the mapped objects, non-final fields and public parameterless constructor.

If these constraints are followed by the user-defined domain objects - MappableMongoOperations can be used which is much
simpler than MongoOperations object:

```java
public interface MappableMongoOperations {
// ...
    String insert(Object object);

    void update(Object object);

    <T> T getById(String id, Class<T> resultClass);
// ...
}
```

Sample usage:

```java
MappableMongoOperations mmo;
//...

Profile profile = new Profile("bob", 36);
final String id = mmo.insert(profile);

profile = new Profile(id, profile);
assertEquals(profile, mmo.getById(Profile.class, id));

profile = new Profile(id, "dave", 47);
mmo.update(profile);
assertEquals(profile, mmo.getById(Profile.class, id));
```
