package org.springframework.mongo.mappable.support;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mongo.mappable.MappableMongoOperations;
import org.springframework.mongo.test.MongoTestSupport;
import org.springframework.mongo.test.objects.*;
import org.springframework.test.context.ContextConfiguration;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.springframework.mongo.support.MongoUtil.withId;

@ContextConfiguration(classes = MappableMongoTemplateTest.Config.class)
public final class MappableMongoTemplateTest extends MongoTestSupport {

    @Autowired
    private MappableMongoOperations mmo;

    @Test
    public void shouldInsertUpdateAndFind() {
        Profile profile = new Profile("bob", 36);
        final String id = mmo.insert(profile);

        profile = new Profile(id, profile);
        assertEquals(profile, mmo.queryById(Profile.class, id));

        profile = new Profile(id, "dave", null);
        mmo.update(profile);
        assertEquals(profile, mmo.queryById(Profile.class, id));
    }

    @Test
    public void shouldRemove() {
        Profile profile = new Profile("bob", 36);
        final String id = mmo.insert(profile);
        assertEquals(Arrays.asList(new Profile(id, profile)), mmo.query(Profile.class, new BasicDBObject()));
        mmo.remove(Profile.class, id);
        assertEquals(Collections.<Profile>emptyList(), mmo.query(Profile.class, new BasicDBObject()));
    }

    @Test
    public void shouldSaveObjectWithList() {
        Shelf shelf = new Shelf(Arrays.asList(new Book("Algebra", 496L)), Arrays.asList("math"), new Profile("ann", 19));
        final String id = mmo.insert(shelf);

        shelf = new Shelf(id, shelf);
        assertEquals(shelf, mmo.queryById(Shelf.class, id));

        shelf = new Shelf(id, Arrays.asList(new Book("Algebra", 496L), new Book("Geo", 85L)),
                Arrays.asList("math", "geometry"), new Profile("jane", 23));
        mmo.update(shelf);
        assertEquals(shelf, mmo.queryById(Shelf.class, id));

        shelf = new Shelf(id, Collections.<Book>emptyList(), Collections.<String>emptyList(), null);
        mmo.update(shelf);
        assertEquals(shelf, mmo.queryById(Shelf.class, id));
    }

    @Test
    public void shouldRemoveObjectByCustomField() {
        Profile profile = new Profile("bob", 36);
        mmo.insert(profile);
        assertFalse(mmo.query(Profile.class, new BasicDBObject()).isEmpty());

        // 1 field
        assertEquals(1, mmo.remove(Profile.class, "name", profile.getName()));
        assertTrue(mmo.query(Profile.class, new BasicDBObject()).isEmpty());

        // insert again
        mmo.insert(profile);
        assertFalse(mmo.query(Profile.class, new BasicDBObject()).isEmpty());

        // remove by 2 fields
        assertEquals(1, mmo.remove(Profile.class, new BasicDBObject("name", profile.getName()).append("age", 36)));
        assertTrue(mmo.query(Profile.class, new BasicDBObject()).isEmpty());
    }

    @Test
    public void shouldQueryForObject() {
        Profile profile = new Profile("bob", 36);
        final String id = mmo.insert(profile);
        profile = new Profile(id, profile);

        assertEquals(profile, mmo.queryForObject(Profile.class, "name", profile.getName()));
        assertEquals(profile, mmo.queryForObject(Profile.class, "age", profile.getAge()));
    }

    @Test
    public void shouldSaveObjectWithNontrivialFields() throws Exception {
        Msg msg = new Msg(MsgState.CREATED, new URL("http://site.com"), URI.create("urn:sample:srv"));
        final String id = mmo.insert(msg);

        msg = new Msg(id, msg);
        assertEquals(msg, mmo.queryById(Msg.class, id));

        msg = new Msg(id, MsgState.SENT, null, null);
        mmo.update(msg);
        assertEquals(msg, mmo.queryById(Msg.class, id));
    }

    @Test
    public void shouldNotRemoveObjectByUnsatisifiedQuery() {
        final Profile profile = new Profile("bob", 36);
        final String id = mmo.insert(profile);
        mmo.remove(Profile.class, new BasicDBObject("name", profile.getName() + "2"));
        assertEquals(new Profile(id, profile), mmo.queryForObject(Profile.class, withId(id)));

        mmo.remove(Profile.class, new BasicDBObject("age", profile.getAge() + 1));
        assertEquals(new Profile(id, profile), mmo.queryForObject(Profile.class,
                new BasicDBObject("age", profile.getAge())));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldRemoveObjectById() {
        Profile profile = new Profile("bob", 36);
        final String id = mmo.insert(profile);
        mmo.remove(Profile.class, id);
        mmo.queryForObject(Profile.class, withId(id));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldRemoveObjectByField() {
        Profile profile = new Profile("bob", 36);
        final String id = mmo.insert(profile);
        mmo.remove(Profile.class, new BasicDBObject("age", profile.getAge()));
        mmo.queryForObject(Profile.class, withId(id));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldRemoveObjectByTwoField() {
        Profile profile = new Profile("bob", 36);
        final String id = mmo.insert(profile);
        mmo.remove(Profile.class, new BasicDBObject().append("age", profile.getAge()).append("name", profile.getName()));
        mmo.queryForObject(Profile.class, withId(id));
    }

    @Configuration
    public static class Config {
        @Autowired
        private DB db;

        @Bean
        public MappableMongoOperations mappableMongoOperations() {
            return new MappableMongoTemplate(TestDomainObject.class, db);
        }
    }
}
