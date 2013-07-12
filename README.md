spring-mongo-min
================

Minimalistic mongo support for Spring-driven projects

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
