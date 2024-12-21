package ru.sfedu.project;

import ru.sfedu.project.db.MongoDatabaseClient;
import ru.sfedu.project.entities.HistoryEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoCollection;

import static com.mongodb.client.model.Filters.eq;


public class HistoryContent {
    private static final MongoCollection<Document> historyCollection;
    static Document historyDoc = new Document();
    private static final Logger log = LogManager.getLogger(HistoryContent.class);

    static {
        try {
            historyCollection = MongoDatabaseClient.getCollection(Constants.MONGO_DB_COLLECTIONS[0]);
        } catch (IOException e) {
            log.error("Error init HistoryContent: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static ObjectId getId() {
        if (historyDoc == null) return null;

        return historyDoc.getObjectId(Constants.MONGO_DB_ID);
    }

    public static void saveHistory(HistoryEntity historyEntity) {
        historyDoc = new Document()
                .append(Constants.MONGO_DB_LOGS_FIELDS[0], historyEntity.getCreationDate())
                .append(Constants.MONGO_DB_LOGS_FIELDS[1], historyEntity.getClassName())
                .append(Constants.MONGO_DB_LOGS_FIELDS[2], historyEntity.getMethodName())
                .append(Constants.MONGO_DB_LOGS_FIELDS[3], historyEntity.getMessage())
                .append(Constants.MONGO_DB_LOGS_FIELDS[4], historyEntity.getStatus())
                .append(Constants.MONGO_DB_LOGS_FIELDS[5], historyEntity.getActor());

        historyCollection.insertOne(historyDoc);
        historyEntity.setId(getId());
    }

    public static HistoryEntity getHistory(ObjectId id) {
        Document doc = historyCollection.find(eq(Constants.MONGO_DB_ID, id)).first();
        if (doc != null) {
            String statusString = doc.getString(Constants.MONGO_DB_LOGS_FIELDS[4]);
            HistoryEntity.Status status = HistoryEntity.Status.valueOf(statusString);

            return new HistoryEntity(
                    doc.getDate(Constants.MONGO_DB_LOGS_FIELDS[0]),
                    doc.getString(Constants.MONGO_DB_LOGS_FIELDS[1]),
                    doc.getString(Constants.MONGO_DB_LOGS_FIELDS[2]),
                    doc.get(Constants.MONGO_DB_LOGS_FIELDS[3]),
                    status,
                    doc.getString(Constants.MONGO_DB_LOGS_FIELDS[5])
            );
        }

        return null;
    }
}
