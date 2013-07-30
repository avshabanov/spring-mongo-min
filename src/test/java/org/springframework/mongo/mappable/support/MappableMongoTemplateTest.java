package org.springframework.mongo.mappable.support;

import com.mongodb.BasicDBObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mongo.core.MongoOperations;
import org.springframework.mongo.core.support.MongoTemplate;
import org.springframework.mongo.mappable.MappableMongoOperations;
import org.springframework.mongo.test.MongoTestSupport;
import org.springframework.mongo.test.objects.*;
import org.springframework.test.context.ContextConfiguration;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

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

    @Configuration
    public static class Config {
        @Bean
        public MappableMongoOperations mappableMongoOperations() {
            return new MappableMongoTemplate(TestDomainObject.class);
        }

        @Bean
        public MongoOperations mongoOperations() {
            return new MongoTemplate();
        }
    }
}
