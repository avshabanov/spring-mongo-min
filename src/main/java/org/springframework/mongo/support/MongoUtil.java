package org.springframework.mongo.support;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;
import org.springframework.dao.DataIntegrityViolationException;
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

    public static BasicDBObject withId(String id) {
        return new BasicDBObject()
                .append(ID, new ObjectId(id, false));
    }

    public static String extractId(DBObject dbObject) {
        final Object idObject = dbObject.get(ID);
        if (idObject == null) {
            throw new DataIntegrityViolationException("No ID field in dbObject=" + dbObject);
        } else if (idObject instanceof ObjectId) {
            return ((ObjectId) idObject).toStringMongod();
        }

        throw new DataIntegrityViolationException("Unrecognized ID field in dbObject=" + dbObject);
    }

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
