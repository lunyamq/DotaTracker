package ru.sfedu.project.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.sfedu.project.Constants;
import ru.sfedu.project.entities.HistoryEntity;
import ru.sfedu.project.utils.ConfigurationUtil;
import ru.sfedu.project.utils.DatabaseUtil;

import java.sql.*;
import java.util.Date;
import java.util.Objects;

public class SqlDatabaseClient {
    private static Connection database;
    private static final String environment = System.getProperty(Constants.CONFIG_KEY, Constants.DEFAULT_CONFIG_PATH);
    private static final ConfigurationUtil util = new ConfigurationUtil(environment);
    private static final Logger log = LogManager.getLogger(SqlDatabaseClient.class);

    static {
        try {
                String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS matches (
                        id BIGINT PRIMARY KEY,
                        duration INT DEFAULT 0,
                        firstBlood INT DEFAULT 0,
                        radiantScore INT DEFAULT 0,
                        direScore INT DEFAULT 0,
                        radiantWin BOOLEAN DEFAULT FALSE,
                        chat TEXT,
                        demoLink VARCHAR(255) DEFAULT NULL,
                        matchDate DATETIME DEFAULT NULL
                    );
                    """;
                Statement stmt = getDatabase().createStatement();
                stmt.executeUpdate(createTableSQL);

    //            createTableSQL = """
    //            CREATE TABLE IF NOT EXISTS match_heroes (
    //                hero_id INT NOT NULL,
    //                hero_name VARCHAR(255) DEFAULT NULL,
    //                match_id BIGINT NOT NULL,
    //                isRadiant BOOLEAN,
    //                PRIMARY KEY (hero_id, match_id),
    //                FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE
    //            );
    //            """;
    //            stmt.executeUpdate(createTableSQL);

                createTableSQL = """
                CREATE TABLE IF NOT EXISTS players (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(255) DEFAULT NULL,
                    rang VARCHAR(50) DEFAULT NULL,
                    wins INT DEFAULT 0,
                    losses INT DEFAULT 0,
                    hasPlus VARCHAR(6) DEFAULT NULL,
                    pings INT DEFAULT 0,
                    rapiers INT DEFAULT 0,
                    observers INT DEFAULT 0,
                    sentries INT DEFAULT 0,
                    towerDmg BIGINT DEFAULT 0,
                    heroDmg BIGINT DEFAULT 0
                );
                """;
                stmt.executeUpdate(createTableSQL);

                createTableSQL = """
                    CREATE TABLE IF NOT EXISTS match_players (
                        player_id BIGINT NOT NULL,
                        match_id BIGINT NOT NULL,
                        player_name VARCHAR(255) DEFAULT NULL,
                        hero_id INT DEFAULT 0,
                        hero_name VARCHAR(255) DEFAULT NULL,
                        is_radiant BOOLEAN DEFAULT FALSE,
                        kills INT DEFAULT 0,
                        deaths INT DEFAULT 0,
                        assists INT DEFAULT 0,
                        PRIMARY KEY (player_id, match_id),
                        FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
                        FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
                    );
                    """;
                stmt.executeUpdate(createTableSQL);
                stmt.close();

            log.info("All tables are created successfully");
        } catch (SQLException e) {
            log.error("Can't create tables: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static Connection getDatabase() {
        if (database == null) {
            try {
                String url = util.getConfigurationEntry(Constants.PARAMS[6]);
                String user = util.getConfigurationEntry(Constants.PARAMS[7]);
                String password = util.getConfigurationEntry(Constants.PARAMS[8]);

                database = DriverManager.getConnection(url, user, password);
                log.info("Connected to SQL database");
            } catch (Exception e) {
                log.error("Error in getDatabase method");
                throw new RuntimeException("Failed to connect to database", e);
            }
        }

        return database;
    }


    private static void putMatchPlayers(String matchId) throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "SqlDatabaseClient",
                "putMatchPlayers()",
                "Saved players for match",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        try {
            JSONObject matchData = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_MATCHES + matchId);
            if (matchData == null) {
                log.error("Empty response: no matchData found");
                historyEntity.setStatus(HistoryEntity.Status.FAIL);
                historyEntity.setMessage("Empty response: no match found");
                DatabaseUtil.save(historyEntity);
                throw new IllegalArgumentException("Match data is null");
            }

            JSONObject heroesJson = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_HEROES);
            if (heroesJson == null) {
                log.error("Heroes data is null in putMatchHeroes");
                throw new IllegalArgumentException("Heroes data is null");
            }

            JSONArray playersData = matchData.getJSONArray("players");
            String query = """
                        INSERT INTO match_players (
                            player_id, match_id, player_name, hero_id, hero_name, is_radiant, kills, deaths, assists
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            player_name = VALUES(player_name),
                            hero_id = VALUES(hero_id),
                            hero_name = VALUES(hero_name),
                            kills = VALUES(kills),
                            deaths = VALUES(deaths),
                            assists = VALUES(assists);
                    """;

            for (int i = 0; i < playersData.length(); i++) {
                JSONObject playerData = playersData.getJSONObject(i);
                if (playerData.has("account_id")) {
                    long playerId = playerData.getLong("account_id");
                    String playerName = playerData.optString("personaname");
                    int heroId = playerData.getInt("hero_id");
                    String heroName = heroesJson.getJSONObject(String.valueOf(heroId)).getString(Constants.MONGO_DB_HEROES_FIELDS[0]);
                    boolean isRadiant = playerData.getBoolean("isRadiant");
                    int kills = playerData.getInt("kills");
                    int deaths = playerData.getInt("deaths");
                    int assists = playerData.getInt("assists");

                    putPlayer(String.valueOf(playerId));

                    try (PreparedStatement preparedStatement = getDatabase().prepareStatement(query)) {
                        preparedStatement.setLong(1, playerId);
                        preparedStatement.setString(2, matchId);
                        preparedStatement.setString(3, playerName);
                        preparedStatement.setInt(4, heroId);
                        preparedStatement.setString(5, heroName);
                        preparedStatement.setBoolean(6, isRadiant);
                        preparedStatement.setInt(7, kills);
                        preparedStatement.setInt(8, deaths);
                        preparedStatement.setInt(9, assists);

                        preparedStatement.executeUpdate();

                    }
                }
            }

            DatabaseUtil.save(historyEntity);
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);

            log.error("Error in savePlayersInMatch method: {}", e.getMessage());
        }
    }

    private static JSONArray getMatchPlayers(String matchId) throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "SqlDatabaseClient",
                "getMatchPlayers()",
                "Receive players from match",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        String query = "SELECT * FROM match_players WHERE match_id = ?";
        JSONArray playersData = new JSONArray();

        try (PreparedStatement stmt = getDatabase().prepareStatement(query)) {
            stmt.setLong(1, Long.parseLong(matchId));
            var resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
                putMatchPlayers(matchId);

                resultSet = stmt.executeQuery();
                if (!resultSet.next()) {
                    log.error("MatchPlayers find error");
                    throw new RuntimeException("Failed to find match data");
                }
            }

            while (resultSet.next()) {
                JSONObject player = new JSONObject();
                player.put("player_id", resultSet.getLong("player_id"));
                player.put("player_name", resultSet.getString("player_name"));
                player.put("hero_id", resultSet.getInt("hero_id"));
                player.put("hero_name", resultSet.getString("hero_name"));
                player.put("is_radiant", resultSet.getBoolean("is_radiant"));
                player.put("kills", resultSet.getInt("kills"));
                player.put("deaths", resultSet.getInt("deaths"));
                player.put("assists", resultSet.getInt("assists"));

                playersData.put(player);
            }

            DatabaseUtil.save(historyEntity);
            return playersData;
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);
            log.error("Error in getMatchPlayers method: {}", e.getMessage());
            return null;
        }
    }

//    private static void putMatchHeroes(String matchId) throws Exception {
//        HistoryEntity historyEntity = new HistoryEntity(
//                new Date(),
//                "SqlDatabaseClient",
//                "putMatchHeroes()",
//                "Saved heroes data for match",
//                HistoryEntity.Status.SUCCESS,
//                Constants.ACTOR_SYSTEM
//        );
//
//        try {
//            JSONObject matchData = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_MATCHES + matchId);
//            if (matchData == null) {
//                log.error("MatchData is null in putMatchHeroes");
//                throw new IllegalArgumentException("MatchData is null");
//            }
//
//            JSONArray playersData = matchData.getJSONArray("players");
//            JSONObject heroesJson = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_HEROES);
//            if (heroesJson == null) {
//                log.error("Heroes data is null in putMatchHeroes");
//                throw new IllegalArgumentException("Heroes data is null");
//            }
//
//            String query = """
//            INSERT INTO match_heroes (hero_id, hero_name, match_id, isRadiant)
//            VALUES (?, ?, ?, ?)
//            ON DUPLICATE KEY UPDATE
//                hero_name = VALUES(hero_name),
//                isRadiant = VALUES(isRadiant);
//            """;
//
//            try (PreparedStatement stmt = getDatabase().prepareStatement(query)) {
//
//                for (int i = 0; i < playersData.length(); i++) {
//                    JSONObject playerData = playersData.getJSONObject(i);
//                    if (playerData.has("hero_id")) {
//                        int heroId = playerData.getInt("hero_id");
//                        boolean isRadiant = playerData.getBoolean("isRadiant");
//                        JSONObject heroData = heroesJson.getJSONObject(String.valueOf(heroId));
//                        String heroName = heroData.getString(Constants.MONGO_DB_HEROES_FIELDS[0]);
//
//                        stmt.setInt(1, heroId);
//                        stmt.setString(2, heroName);
//                        stmt.setLong(3, Long.parseLong(matchId));
//                        stmt.setBoolean(4, isRadiant);
//
//                        stmt.executeUpdate();
//                    }
//                }
//            }
//
//            DatabaseUtil.save(historyEntity);
//            log.info("Heroes data for match saved successfully");
//        } catch (Exception e) {
//            historyEntity.setStatus(HistoryEntity.Status.FAIL);
//            historyEntity.setMessage(e.getMessage());
//            DatabaseUtil.save(historyEntity);
//
//            log.error("Error in putMatchHeroes method: {}", e.getMessage());
//        }
//    }

//    private static JSONArray getMatchHeroes(String matchId) throws Exception {
//        HistoryEntity historyEntity = new HistoryEntity(
//                new Date(),
//                "SqlDatabaseClient",
//                "getMatchHeroes()",
//                "Receive heroes data for match",
//                HistoryEntity.Status.SUCCESS,
//                Constants.ACTOR_SYSTEM
//        );
//
//        String checkQuery = """
//            SELECT hero_id, hero_name, isRadiant
//            FROM match_heroes
//            WHERE match_id = ?
//            """;
//
//        JSONArray heroesArray = new JSONArray();
//        try (PreparedStatement checkStmt = getDatabase().prepareStatement(checkQuery)) {
//            checkStmt.setLong(1, Long.parseLong(matchId));
//            var resultSet = checkStmt.executeQuery();
//
//            if (!resultSet.next()) {
//                putMatchHeroes(matchId);
//
//                resultSet = checkStmt.executeQuery();
//                if (!resultSet.next()) {
//                    log.error("MatchHeroes find error");
//                    throw new RuntimeException("Failed to find match data");
//                }
//            }
//
//            while (resultSet.next()) {
//                JSONObject heroData = new JSONObject();
//                heroData.put("hero_id", resultSet.getInt("hero_id"));
//                heroData.put("hero_name", resultSet.getString("hero_name"));
//                heroData.put("isRadiant", resultSet.getBoolean("isRadiant"));
//
//                heroesArray.put(heroData);
//            }
//
//            DatabaseUtil.save(historyEntity);
//            return heroesArray;
//        } catch (Exception e) {
//            historyEntity.setStatus(HistoryEntity.Status.FAIL);
//            historyEntity.setMessage(e.getMessage());
//            DatabaseUtil.save(historyEntity);
//
//            log.error("Error in getMatchHeroes method: {}", e.getMessage());
//            return null;
//        }
//    }

    private static void putMatch(String matchId) throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "SqlDatabaseClient",
                "putMatch()",
                "Saved match by id",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        try {
            JSONObject matchData = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_MATCHES + matchId);
            if (matchData == null) {
                log.error("MatchData is null");
                throw new IllegalArgumentException("MatchData is null");
            }

            long id = matchData.getLong("match_id");
            int duration = matchData.getInt("duration");
            int firstBlood = matchData.getInt("first_blood_time");
            int radiantScore = matchData.getInt("radiant_score");
            int direScore = matchData.getInt("dire_score");
            boolean isRadiantWin = matchData.getBoolean("radiant_win");
            String chat = matchData.has("chat") ? matchData.getJSONArray("chat").toString() : null;
            String demoLink = matchData.has("replay_url") ? matchData.optString("replay_url") : null;
            long matchDate = matchData.getLong("start_time");

            String query = """
                INSERT INTO matches (id, duration, firstBlood, radiantScore, direScore,
                radiantWin, chat, demoLink, matchDate)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, FROM_UNIXTIME(?))
                ON DUPLICATE KEY UPDATE
                duration = VALUES(duration),
                firstBlood = VALUES(firstBlood),
                radiantScore = VALUES(radiantScore),
                direScore = VALUES(direScore),
                radiantWin = VALUES(radiantWin),
                chat = VALUES(chat),
                demoLink = VALUES(demoLink),
                matchDate = VALUES(matchDate);
                """;
            try (PreparedStatement preparedStatement = getDatabase().prepareStatement(query)) {
                preparedStatement.setLong(1, id);
                preparedStatement.setInt(2, duration);
                preparedStatement.setInt(3, firstBlood);
                preparedStatement.setInt(4, radiantScore);
                preparedStatement.setInt(5, direScore);
                preparedStatement.setBoolean(6, isRadiantWin);
                preparedStatement.setString(7, chat);
                preparedStatement.setString(8, demoLink);
                preparedStatement.setLong(9, matchDate);

                preparedStatement.executeUpdate();
            }

            putMatchPlayers(matchId);
            DatabaseUtil.save(historyEntity);
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);

            log.error("Error in putMatch method: {}", e.getMessage());
        }
    }

    private static JSONObject getMatch(String matchId) throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "SqlDatabaseClient",
                "getMatch()",
                "Find match by id",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        String checkQuery = "SELECT * FROM matches WHERE id = ?";
        try (PreparedStatement checkStmt = getDatabase().prepareStatement(checkQuery)) {
            checkStmt.setLong(1, Long.parseLong(matchId));
            var resultSet = checkStmt.executeQuery();

            if (!resultSet.next()) {
                putMatch(matchId);

                resultSet = checkStmt.executeQuery();
                if (!resultSet.next()) {
                    log.error("Match find error");
                    throw new RuntimeException("Failed to find match data");
                }
            }

            JSONObject matchData = new JSONObject();
            matchData.put("id", resultSet.getLong("id"));
            matchData.put("duration", resultSet.getInt("duration"));
            matchData.put("firstBlood", resultSet.getInt("firstBlood"));
            matchData.put("radiantScore", resultSet.getInt("radiantScore"));
            matchData.put("direScore", resultSet.getInt("direScore"));
            matchData.put("radiantWin", resultSet.getBoolean("radiantWin"));
            matchData.put("chat", resultSet.getString("chat"));
            matchData.put("demoLink", resultSet.getString("demoLink"));
            matchData.put("matchDate", resultSet.getTimestamp("matchDate"));

            return matchData;
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);

            log.error("Error in getMatch method: {}", e.getMessage());
            return null;
        }
    }

    public static void deleteMatchData(String matchId) throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "SqlDatabaseClient",
                "deleteMatch()",
                "Delete match by ID",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        String deleteQuery = "DELETE FROM matches WHERE id = ?";
        try (PreparedStatement stmt = getDatabase().prepareStatement(deleteQuery)) {
            stmt.setLong(1, Long.parseLong(matchId));
            int count = stmt.executeUpdate();
            if (count > 0)
                log.info("Match with ID {} deleted successfully", matchId);
            else
                log.warn("No match found with ID {} to delete", matchId);

            DatabaseUtil.save(historyEntity);
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);
            log.error("Error in deleteMatch method: {}", e.getMessage());
        }
    }

    public static JSONObject getMatchData(String matchId) throws Exception {
        JSONObject matchData = getMatch(matchId);
        JSONArray playersData = getMatchPlayers(matchId);

        if (matchData != null)
            matchData.put("players", playersData);

        return matchData;
    }

    public static void putPlayer(String playerId) throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "SqlDatabaseClient",
                "putPlayer()",
                "Saved player by id",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        try {
            JSONObject playerData = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_PLAYERS + playerId);
            if (playerData == null) {
                log.error("playerData is null");
                throw new IllegalArgumentException("playerData is null");
            }

            JSONObject profile = playerData.getJSONObject("profile");
            if (profile == null) {
                log.error("profile is null");
                throw new IllegalArgumentException("profile is null");
            }

            JSONObject playerDataWL = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_PLAYERS + playerId + Constants.API_CONSTANTS_PLAYERS_WL);
            if (playerDataWL == null) {
                log.error("playerDataWL is null");
                throw new IllegalArgumentException("playerDataWL is null");
            }

            JSONArray playerDataSum = DatabaseUtil.getJsonArr(Constants.API_CONSTANTS_PLAYERS + playerId + Constants.API_CONSTANTS_PLAYERS_SUM);
            if (playerDataSum == null) {
                log.error("playerDataSum is null");
                throw new IllegalArgumentException("playerDataSum is null");
            }

            JSONObject playerDataChat = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_PLAYERS + playerId + "/wordcloud");
            if (playerDataChat == null) {
                log.error("playerDataChat is null");
                throw new IllegalArgumentException("playerDataChat is null");
            }

            long id = profile.getLong("account_id");
            String name = profile.optString("personaname");
            String rang = playerData.optString("rank_tier");
            int wins = playerDataWL.getInt("win");
            int losses = playerDataWL.getInt("lose");
            String hasPlus = profile.getBoolean("plus") ? "true" : "false";
            int pings = 0;
            int rapiers = 0;
            int observers = 0;
            int sentries = 0;
            long towerDmg = 0;
            long heroDmg = 0;

            for (int i = 0; i < playerDataSum.length(); i++) {
                JSONObject total = playerDataSum.getJSONObject(i);
                String field = total.getString("field");
                int sum = total.getInt("sum");

                switch (field) {
                    case "pings":
                        pings = sum;
                        break;
                    case "purchase_rapier":
                        rapiers = sum;
                        break;
                    case "purchase_ward_observer":
                        observers = sum;
                        break;
                    case "purchase_ward_sentry":
                        sentries = sum;
                        break;
                    case "tower_damage":
                        towerDmg = sum;
                        break;
                    case "hero_damage":
                        heroDmg = sum;
                        break;
                    default:
                        break;
                }
            }

            String query = """
            INSERT INTO players (
                id, name, rang, wins, losses, hasPlus, pings, rapiers,
                observers, sentries, towerDmg, heroDmg
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                rang = VALUES(rang),
                wins = VALUES(wins),
                losses = VALUES(losses),
                hasPlus = VALUES(hasPlus),
                pings = VALUES(pings),
                rapiers = VALUES(rapiers),
                observers = VALUES(observers),
                sentries = VALUES(sentries),
                towerDmg = VALUES(towerDmg),
                heroDmg = VALUES(heroDmg)
            """;

            try (PreparedStatement stmt = getDatabase().prepareStatement(query)) {
                stmt.setLong(1, id);
                stmt.setString(2, name);
                stmt.setString(3, rang);
                stmt.setInt(4, wins);
                stmt.setInt(5, losses);
                stmt.setString(6, hasPlus);
                stmt.setInt(7, pings);
                stmt.setInt(8, rapiers);
                stmt.setInt(9, observers);
                stmt.setInt(10, sentries);
                stmt.setLong(11, towerDmg);
                stmt.setLong(12, heroDmg);
                stmt.executeUpdate();
            }

            DatabaseUtil.save(historyEntity);
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);

            log.error("Error in putPlayer method: {}", e.getMessage());
        }
    }
}
