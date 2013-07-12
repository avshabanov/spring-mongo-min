package org.springframework.mongo.support;

import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.mongo.UncategorizedMongoException;

/**
 * Mongo Utility class
 *
 * @author Alexander Shabanov
 */
public final class MongoUtil {

    /**
     * Default ID name
     */
    public static final String ID = "_id";

    public static WriteResult executeWriteOperation(MongoWriteOperation writeOperation) {
        try {
            final WriteResult result = writeOperation.execute();
            final MongoException mongoException = result.getLastError().getException();
            if (mongoException != null) {
                throw mongoException;
            }

            return result;
        } catch (MongoException.DuplicateKey e) {
            throw new DuplicateKeyException("Mongo PK violation", e);
        } catch (MongoException e) {
            throw new UncategorizedMongoException("Unable to insert new record", e);
        }
    }

    public static void expectOneUpdate(WriteResult result) {
        if (result.getN() == 0) {
            throw new IncorrectUpdateSemanticsDataAccessException("Update failed: " +
                    "expecting to update exactly one record, result=" + result);
        }
    }

    private MongoUtil() {}
}
