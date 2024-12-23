package ru.sfedu.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.json.JSONObject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import ru.sfedu.project.db.MongoDatabaseClient;
import ru.sfedu.project.db.SqlDatabaseClient;
import ru.sfedu.project.entities.HistoryEntity;
import ru.sfedu.project.utils.DatabaseUtil;

import java.util.Date;
import java.util.concurrent.Callable;

@Command(name = "DotaCLI", mixinStandardHelpOptions = true, version = "DotaCLI 1.0",
        description = "CLI for DotaTracker")
public class DotaCLI implements Callable<Integer> {
    private static final Logger log = LogManager.getLogger(DotaCLI.class);
    JsonWriterSettings settings = JsonWriterSettings.builder().indent(true).build();

    @Option(names = {"-r", "--refresh-db"}, description = "Refresh the MongoDB")
    private boolean refreshDb;
    @Option(names = {"-hr", "--hero"}, description = "Get hero stat by Name")
    private String heroName;
    @Option(names = {"-pt", "--patch"}, description = "Get patch info by Name")
    private String patchName;

    @Option(names = {"-p", "--player"}, description = "Get player info by Id")
    private String playerIdAdd;
    @Option(names = {"-pd", "--player-dell"}, description = "Delete player info by Id")
    private String playerIdDell;
    @Option(names = {"-m", "--match"}, description = "Get match info by Id")
    private String matchIdAdd;
    @Option(names = {"-md", "--match-dell"}, description = "Delete match info by Id")
    private String matchIdDell;

    @Override
    public Integer call() throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "DotaCLI",
                "call()",
                "CLI",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        try {
            // TODO: del trash text
            if (refreshDb) {
                log.info("Refreshing MongoDB...");
                MongoDatabaseClient.putHeroes();
                MongoDatabaseClient.putPatches();
                log.info("Refresh done");
            }
            if (heroName != null) {
                Document hero = MongoDatabaseClient.getHero(heroName);
                if (hero != null)
                    log.info(hero.toJson(settings));
                else
                    log.error("Hero does not exist or can not be find in database\n" +
                            "Please refresh the MongoDB(-r) and try again");
            }
            if (patchName != null) {
                Document patch = MongoDatabaseClient.getPatch(patchName);
                if (patch != null)
                    log.info(patch.toJson(settings));
                else
                    log.error("Patch does not exist or can not be find in database\n" +
                            "Please refresh the MongoDB(-r) and try again");
            }

            if (playerIdAdd != null) {
                JSONObject player = SqlDatabaseClient.getPlayerData(playerIdAdd);
                if (player != null)
                    log.info(player.toString(2));
                else
                    log.error("Player does not exist");
            }
            if (playerIdDell != null) {
                log.info("Deleting player from DB...");
                int count = SqlDatabaseClient.deletePlayerData(playerIdDell);
                log.info("Deleted {} rows", count);
            }
            if (matchIdAdd != null) {
                JSONObject match = SqlDatabaseClient.getMatchData(matchIdAdd);
                if (match != null)
                    log.info(match.toString(2));
                else
                    log.error("Match does not exist");
            }
            if (matchIdDell != null) {
                log.info("Deleting match from DB...");
                int count = SqlDatabaseClient.deleteMatchData(matchIdDell);
                log.info("Deleted {} rows", count);
            }
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);
            log.error("Error in call method: {}", e.getMessage());
            return 1;
        }

        return 0;
    }
}