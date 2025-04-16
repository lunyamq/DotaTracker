package ru.sfedu.project;

import static ru.sfedu.project.Constants.log;

import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.json.JSONObject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import ru.sfedu.project.db.MongoDatabaseClient;
import ru.sfedu.project.db.SqlDatabaseClient;
import ru.sfedu.project.entities.HistoryEntity;
import ru.sfedu.project.utils.DatabaseUtil;
import java.util.concurrent.Callable;

@Command(name = "DotaCLI", mixinStandardHelpOptions = true, version = "DotaCLI 1.0",
        description = "CLI for DotaTracker")
public class DotaCLI implements Callable<Integer> {
    JsonWriterSettings settings = JsonWriterSettings.builder().indent(true).build();

    @Option(names = {"-r", "--refresh-db"}, description = "Refresh the MongoDB")
    private boolean refreshDb;
    @Option(names = {"-hr", "--hero"}, description = "Get hero stat by Name")
    private String heroName;
    @Option(names = {"-pt", "--patch"}, description = "Get patch info by Name")
    private String patchName;

    @Option(names = {"-c", "--cluster"}, description = "Use MongoDB on cluster(local by def). Slower. Works with -r, -hr, -pt")
    private boolean cluster;

    @Option(names = {"-p", "--player"}, description = "Get player info by Id")
    private String playerIdAdd;
    @Option(names = {"-pd", "--player-dell"}, description = "Delete player info by Id")
    private String playerIdDell;
    @Option(names = {"-m", "--match"}, description = "Get match info by Id")
    private String matchIdAdd;
    @Option(names = {"-md", "--match-dell"}, description = "Delete match info by Id")
    private String matchIdDell;

    @Override
    public Integer call() {
        HistoryEntity historyEntity = HistoryEntity.init("DotaCLI", "call()", "CLI");

        try {
            if (refreshDb) {
                if (cluster)
                    MongoDatabaseClient.getDatabase(true);

                log.info("Refreshing MongoDB...");
                MongoDatabaseClient.putHeroes();
                MongoDatabaseClient.putPatches();
                log.info("Refresh done");
            }
            if (heroName != null) {
                if (cluster)
                    MongoDatabaseClient.getDatabase(true);

                Document hero = MongoDatabaseClient.getHero(heroName);
                if (!hero.isEmpty())
                    log.info(hero.toJson(settings));
                else
                    log.error("Hero does not exist or can not be find in database\n" +
                            "Please refresh the MongoDB(-r) and try again");
            }
            if (patchName != null) {
                if (cluster)
                    MongoDatabaseClient.getDatabase(true);

                Document patch = MongoDatabaseClient.getPatch(patchName);
                if (!patch.isEmpty())
                    log.info(patch.toJson(settings));
                else
                    log.error("Patch does not exist or can not be find in database\n" +
                            "Please refresh the MongoDB(-r) and try again");
            }

            if (playerIdAdd != null) {
                JSONObject player = SqlDatabaseClient.getPlayerData(playerIdAdd);
                if (!player.isEmpty())
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
                if (!match.isEmpty())
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

            return 3;
        }

        return 0;
    }
}