package org.springframework.mongo.test.config;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.Ignore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DB configuration for junit tests
 *
 * @author Alexander Shabanov
 */
@Ignore
@Configuration
public class TestEmbeddedDbConfig {
    private static final int PORT = 16544;

    @Bean
    public MongodStarter mongodStarter() {
        return MongodStarter.getDefaultInstance();
    }

    @Bean(destroyMethod = "stop")
    public MongodExecutable mongodExecutable() throws Exception {
        return mongodStarter().prepare(new MongodConfig(
                Version.Main.PRODUCTION, PORT, Network.localhostIsIPv6()));
    }

    @Bean(destroyMethod = "stop")
    public MongodProcess mongodProcess() throws Exception {
        return mongodExecutable().start();
    }

    @Bean
    public MongoClient mongoClient() throws Exception {
        return new MongoClient("127.0.0.1", PORT);
    }

    @Bean
    public DB db() throws Exception {
        return mongoClient().getDB("springMongoTest");
    }
}
