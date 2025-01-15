package ru.sfedu.project.db;

import static ru.sfedu.project.Constants.log;
import static ru.sfedu.project.Constants.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.core.config.Configurator;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.sfedu.project.Constants;
import ru.sfedu.project.entities.HistoryEntity;
import ru.sfedu.project.utils.DatabaseUtil;
import java.io.IOException;
import java.util.Date;
import static com.mongodb.client.model.Filters.eq;

public class MongoDatabaseClient {
    private static MongoDatabase database = null;

    static {
        Configurator.setLevel("org.mongodb", org.apache.logging.log4j.Level.OFF);
    }

    public static MongoDatabase getDatabase() throws IOException {
        if (database == null) {
            try {
                String uri = util.getConfigurationEntry(Constants.PARAMS[4]);
                MongoClient mongoClient = MongoClients.create(uri);
                database = mongoClient.getDatabase(Constants.MONGO_DB_NAME);
            } catch (Exception e) {
                log.error("Error in getDatabase method");
                throw new RuntimeException("Failed to connect to database", e);
            }
        }

        return database;
    }

    public static MongoCollection<Document> getCollection(String collectionName) throws IOException {
        return getDatabase().getCollection(collectionName);
    }

    public static void putHeroes() throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "MongoDatabaseClient",
                "putHeroes()",
                "Saved all heroes",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        try {
            JSONObject heroesJson = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_HEROES);

            MongoCollection<Document> collection = MongoDatabaseClient.getCollection(Constants.MONGO_DB_COLLECTIONS[1]);
            collection.deleteMany(new Document());
            for (String heroId : heroesJson.keySet()) {
                JSONObject heroData = heroesJson.getJSONObject(heroId);
                String heroName = heroData.getString(Constants.MONGO_DB_HEROES_FIELDS[0]);

                Document heroDocument = new Document()
                        .append(Constants.MONGO_DB_ID, heroId)
                        .append(Constants.MONGO_DB_HEROES_FIELDS[0], heroName)
                        .append(Constants.MONGO_DB_HEROES_FIELDS[1], heroData.toMap());

                collection.insertOne(heroDocument);
            }

            DatabaseUtil.save(historyEntity);
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);

            log.error("Error in putHeroes method: {}", e.getMessage());
        }
    }

    public static Document getHero(String name) throws IOException {
        MongoCollection<Document> collection = MongoDatabaseClient.getCollection(Constants.MONGO_DB_COLLECTIONS[1]);
        Document res = collection.find(eq(Constants.MONGO_DB_HEROES_FIELDS[0], name)).first();
        return res != null ? res : new Document();
    }

    public static void putPatches() throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "MongoDatabaseClient",
                "putPatches()",
                "Saved all patches",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        try {
            JSONArray patchJsonArr = DatabaseUtil.getJsonArr(Constants.API_CONSTANTS_PATCHES);

            MongoCollection<Document> collection = MongoDatabaseClient.getCollection(Constants.MONGO_DB_COLLECTIONS[2]);
            collection.deleteMany(new Document());
            for (int i = 0; i < patchJsonArr.length(); i++) {
                JSONObject patchData = patchJsonArr.getJSONObject(i);
                Integer patchId = patchData.getInt(Constants.MONGO_DB_PATCHES_FIELDS[0]);
                String patchName = patchData.getString(Constants.MONGO_DB_PATCHES_FIELDS[1]);
                String patchDate = patchData.getString(Constants.MONGO_DB_PATCHES_FIELDS[2]);

                Document heroDocument = new Document()
                        .append(Constants.MONGO_DB_ID, patchId)
                        .append(Constants.MONGO_DB_PATCHES_FIELDS[1], patchName)
                        .append(Constants.MONGO_DB_PATCHES_FIELDS[2], patchDate);

                collection.insertOne(heroDocument);
            }

            DatabaseUtil.save(historyEntity);
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);

            log.error("Error in putPatches method: {}", e.getMessage());
        }
    }

    public static Document getPatch(String name) throws IOException {
        MongoCollection<Document> collection = MongoDatabaseClient.getCollection(Constants.MONGO_DB_COLLECTIONS[2]);
        Document res = collection.find(eq(Constants.MONGO_DB_PATCHES_FIELDS[1], name)).first();
        return res != null ? res : new Document();
    }
}
