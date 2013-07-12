package org.springframework.mongo.mappable.support;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mongo.core.MongoOperations;
import org.springframework.mongo.core.support.MongoTemplate;
import org.springframework.mongo.mappable.MappableMongoOperations;
import org.springframework.mongo.test.MongoTestSupport;
import org.springframework.mongo.test.objects.Profile;
import org.springframework.test.context.ContextConfiguration;

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
        assertEquals(profile, mmo.getById(id, Profile.class));

        profile = new Profile(id, "dave", 47);
        mmo.update(profile);
        assertEquals(profile, mmo.getById(id, Profile.class));
    }

    @Configuration
    public static class Config {
        @Bean
        public MappableMongoOperations mappableMongoOperations() {
            return new MappableMongoTemplate();
        }

        @Bean
        public MongoOperations mongoOperations() {
            return new MongoTemplate();
        }
    }
}
