package org.springframework.mongo.test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mongo.test.config.TestHybridDbConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Alexander Shabanov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestHybridDbConfig.class})
public abstract class MongoTestSupport {
    @Autowired
    private DB db;

    @After
    public void cleanup() {
        for (final String name : db.getCollectionNames()) {
            if (!Character.isUpperCase(name.charAt(0))) {
                continue; // our table names start with uppercase character
            }
            db.getCollection(name).remove(new BasicDBObject());
        }
    }
}
