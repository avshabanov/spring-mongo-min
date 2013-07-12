package org.springframework.mongo.test.config;

import com.mongodb.DB;
import com.mongodb.Mongo;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.junit.Ignore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Ignore
@Configuration
public class TestHybridDbConfig {
    @Bean(destroyMethod = "shutdown")
    public MongodForTestsFactory factory() throws IOException {
        return MongodForTestsFactory.with(Version.Main.PRODUCTION);
    }

    @Bean
    public Mongo mongo() throws IOException {
        return factory().newMongo();
    }

    @Bean
    public DB db() throws IOException {
        return mongo().getDB("springMongoTest");
    }
}
