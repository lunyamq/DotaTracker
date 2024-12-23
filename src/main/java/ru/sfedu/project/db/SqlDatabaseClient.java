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

public class SqlDatabaseClient {
    private static Connection database;
    private static final String environment = System.getProperty(Constants.CONFIG_KEY, Constants.DEFAULT_CONFIG_PATH);
    private static final ConfigurationUtil util = new ConfigurationUtil(environment);
    private static final Logger log = LogManager.getLogger(SqlDatabaseClient.class);

    static {
        try {
                String createTableSQL = Constants.SQL_MATCHES_TABLE;
                Statement stmt = getDatabase().createStatement();
                stmt.executeUpdate(createTableSQL);

                createTableSQL = Constants.SQL_PLAYERS_TABLE;
                stmt.executeUpdate(createTableSQL);

                createTableSQL = Constants.SQL_MATCH_PLAYERS_TABLE;
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
            JSONObject heroesJson = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_HEROES);

            if (matchData == null || heroesJson == null) {
                log.error("Empty JSON response: no matchData or heroesJson found");
                throw new IllegalArgumentException("matchData or heroesJson is null");
            }

            JSONArray playersData = matchData.getJSONArray(Constants.JSON_PLAYER);
            String query = Constants.SQL_MATCH_PLAYERS_INSERT;
            for (int i = 0; i < playersData.length(); i++) {
                JSONObject playerData = playersData.getJSONObject(i);
                if (playerData.has(Constants.JSON_ACCOUNT)) {
                    long playerId = playerData.getLong(Constants.JSON_ACCOUNT);
                    String playerName = playerData.optString(Constants.JSON_NAME);
                    int heroId = playerData.getInt(Constants.JSON_HERO);
                    String heroName = heroesJson.getJSONObject(String.valueOf(heroId)).getString(Constants.MONGO_DB_HEROES_FIELDS[0]);
                    boolean isRadiant = playerData.getBoolean(Constants.JSON_TEAM);
                    int kills = playerData.getInt(Constants.JSON_KDA[0]);
                    int deaths = playerData.getInt(Constants.JSON_KDA[1]);
                    int assists = playerData.getInt(Constants.JSON_KDA[2]);

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

        String query = Constants.SQL_MATCH_PLAYERS_SELECT;
        JSONArray playersData = new JSONArray();

        try (PreparedStatement stmt = getDatabase().prepareStatement(query)) {
            stmt.setLong(1, Long.parseLong(matchId));
            var resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
                putMatchPlayers(matchId);

                resultSet = stmt.executeQuery();
                if (!resultSet.next()) {
                    log.error("Empty JSON response: no resultSet found");
                    throw new RuntimeException("resultSet is null");
                }
            }

            while (resultSet.next()) {
                JSONObject player = new JSONObject();
                player.put(Constants.JSON_PLAYER_ID, resultSet.getLong(Constants.JSON_PLAYER_ID));
                player.put(Constants.JSON_PLAYER_NAME, resultSet.getString(Constants.JSON_PLAYER_NAME));
                player.put(Constants.JSON_HERO, resultSet.getInt(Constants.JSON_HERO));
                player.put(Constants.JSON_HERO_NAME, resultSet.getString(Constants.JSON_HERO_NAME));
                player.put(Constants.JSON_IS_RADIANT, resultSet.getBoolean(Constants.JSON_IS_RADIANT));
                player.put(Constants.JSON_KDA[0], resultSet.getInt(Constants.JSON_KDA[0]));
                player.put(Constants.JSON_KDA[1], resultSet.getInt(Constants.JSON_KDA[1]));
                player.put(Constants.JSON_KDA[2], resultSet.getInt(Constants.JSON_KDA[2]));

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
                log.error("Empty JSON response: no matchData found");
                throw new IllegalArgumentException("matchData is null");
            }

            long id = matchData.getLong(Constants.JSON_MATCH);
            int duration = matchData.getInt(Constants.JSON_DURATION);
            int firstBlood = matchData.getInt(Constants.JSON_FB);
            int radiantScore = matchData.getInt(Constants.JSON_SCORE[0]);
            int direScore = matchData.getInt(Constants.JSON_SCORE[1]);
            boolean isRadiantWin = matchData.getBoolean(Constants.JSON_WINNER);
            String chat = matchData.has(Constants.JSON_CHAT) ? matchData.getJSONArray(Constants.JSON_CHAT).toString() : null;
            String demoLink = matchData.has(Constants.JSON_REPLAY) ? matchData.optString(Constants.JSON_REPLAY) : null;
            long matchDate = matchData.getLong(Constants.JSON_START);

            String query = Constants.SQL_MATCHES_INSERT;
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

        String checkQuery = Constants.SQL_MATCH_SELECT;
        try (PreparedStatement checkStmt = getDatabase().prepareStatement(checkQuery)) {
            checkStmt.setLong(1, Long.parseLong(matchId));
            var resultSet = checkStmt.executeQuery();

            if (!resultSet.next()) {
                putMatch(matchId);

                resultSet = checkStmt.executeQuery();
                if (!resultSet.next()) {
                    log.error("Empty JSON response: no resultSet found ");
                    throw new RuntimeException("resultSet is null");
                }
            }

            JSONObject matchData = new JSONObject();
            matchData.put(Constants.JSON_ID, resultSet.getLong(Constants.JSON_ID));
            matchData.put(Constants.JSON_DURATION, resultSet.getInt(Constants.JSON_DURATION));
            matchData.put(Constants.JSON_FIRST_BLOOD, resultSet.getInt(Constants.JSON_FIRST_BLOOD));
            matchData.put(Constants.JSON_TEAM_SCORE[0], resultSet.getInt(Constants.JSON_TEAM_SCORE[0]));
            matchData.put(Constants.JSON_TEAM_SCORE[1], resultSet.getInt(Constants.JSON_TEAM_SCORE[1]));
            matchData.put(Constants.JSON_TEAM_WINNER, resultSet.getBoolean(Constants.JSON_TEAM_WINNER));
            matchData.put(Constants.JSON_CHAT, resultSet.getString(Constants.JSON_CHAT));
            matchData.put(Constants.JSON_DEMO_LINK, resultSet.getString(Constants.JSON_DEMO_LINK));
            matchData.put(Constants.JSON_MATCH_DATE, resultSet.getTimestamp(Constants.JSON_MATCH_DATE));

            return matchData;
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);

            log.error("Error in getMatch method: {}", e.getMessage());
            return null;
        }
    }

    public static int deleteMatchData(String matchId) throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "SqlDatabaseClient",
                "deleteMatch()",
                "Delete match by ID",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        String deleteQuery = Constants.SQL_MATCH_DELETE;
        try (PreparedStatement stmt = getDatabase().prepareStatement(deleteQuery)) {
            stmt.setLong(1, Long.parseLong(matchId));
            int count = stmt.executeUpdate();
            if (count > 0)
                log.info("Match deleted successfully");
            else
                log.warn("No match found to delete");

            DatabaseUtil.save(historyEntity);
            return count;
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);
            log.error("Error in deleteMatch method: {}", e.getMessage());
            return -1;
        }
    }

    public static JSONObject getMatchData(String matchId) throws Exception {
        JSONObject matchData = getMatch(matchId);
        JSONArray playersData = getMatchPlayers(matchId);

        if (matchData != null)
            matchData.put(Constants.JSON_PLAYER, playersData);

        return matchData;
    }

    private static void putPlayer(String playerId) throws Exception {
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
                log.error("Empty JSON response: no playerData found");
                throw new IllegalArgumentException("playerData is null");
            }

            JSONObject profile = playerData.getJSONObject("profile");
            JSONObject playerDataWL = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_PLAYERS + playerId + Constants.API_CONSTANTS_PLAYERS_WL);
            JSONArray playerDataSum = DatabaseUtil.getJsonArr(Constants.API_CONSTANTS_PLAYERS + playerId + Constants.API_CONSTANTS_PLAYERS_SUM);
            JSONObject playerDataChat = DatabaseUtil.getJsonObj(Constants.API_CONSTANTS_PLAYERS + playerId + "/wordcloud");
            if (profile == null || playerDataWL == null || playerDataSum == null || playerDataChat == null) {
                log.error("Empty JSON response: no profile or playerDataWL or playerDataSum or playerDataChat found");
                throw new IllegalArgumentException("profile or playerDataWL or playerDataSum or playerDataChat is null");
            }

            String rank = playerData.optString(Constants.JSON_RANK);
            if (rank.length() < 2) {
                log.error("rank length error");
                throw new RuntimeException("unknown rank");
            }
            int league = Character.getNumericValue(rank.charAt(0));
            int tier = Character.getNumericValue(rank.charAt(1));
            int startMMR = -1;
            int endMMR = -1;
            if (league <= 6 && league >= 1) {
                startMMR = ((league - 1) * 5 + (tier - 1)) * 154;
                endMMR = startMMR + 154;
                rank = Constants.RANK_NAMES[league - 1] + " " + tier + ": " + startMMR + " - " + endMMR;
            } else if (league == 7) {
                startMMR = 4620 + (tier - 1) * 200;
                endMMR = startMMR + 200;
                rank = Constants.RANK_NAMES[league - 1] + " " + tier + ": " + startMMR + " - " + endMMR;
            } else if (league == 8) {
                startMMR = 5620;
                rank = Constants.RANK_NAMES[league - 1] + ": " + startMMR + "+";
            } else {
                log.error("unknown rank");
                throw new RuntimeException("unknown rank");
            }

            long id = profile.getLong(Constants.JSON_ACCOUNT);
            String name = profile.optString(Constants.JSON_NAME);
            String rang = rank;
            int wins = playerDataWL.getInt(Constants.JSON_WIN);
            int losses = playerDataWL.getInt(Constants.JSON_LOSE);
            String hasPlus = profile.getBoolean(Constants.JSON_PLUS) ? "true" : "false";
            int pings = 0;
            int rapiers = 0;
            int observers = 0;
            int sentries = 0;
            long towerDmg = 0;
            long heroDmg = 0;

            for (int i = 0; i < playerDataSum.length(); i++) {
                JSONObject total = playerDataSum.getJSONObject(i);
                String field = total.getString(Constants.JSON_FIELD);
                int sum = total.getInt(Constants.JSON_SUM);

                // TODO: switch -> stream
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

            String query = Constants.SQL_PLAYERS_INSERT;
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

    public static JSONObject getPlayerData(String playerId) throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "SqlDatabaseClient",
                "getPlayerData()",
                "Find player data by id",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        String checkQuery = Constants.SQL_PLAYER_SELECT;
        try (PreparedStatement checkStmt = getDatabase().prepareStatement(checkQuery)) {
            checkStmt.setLong(1, Long.parseLong(playerId));
            var resultSet = checkStmt.executeQuery();

            if (!resultSet.next()) {
                putPlayer(playerId);

                resultSet = checkStmt.executeQuery();
                if (!resultSet.next()) {
                    log.error("Empty JSON response: no resultSet found  ");
                    throw new RuntimeException("resultSet is null");
                }
            }

            JSONObject playerData = new JSONObject();
            playerData.put(Constants.JSON_ID, resultSet.getLong(Constants.JSON_ID));
            playerData.put(Constants.JSON_ACCOUNT_NAME, resultSet.getString(Constants.JSON_ACCOUNT_NAME));
            playerData.put(Constants.JSON_RANG, resultSet.getString(Constants.JSON_RANG));
            playerData.put(Constants.JSON_WINS, resultSet.getInt(Constants.JSON_WINS));
            playerData.put(Constants.JSON_LOSES, resultSet.getInt(Constants.JSON_LOSES));
            playerData.put(Constants.JSON_HAS_PLUS, resultSet.getString(Constants.JSON_HAS_PLUS));
            playerData.put(Constants.JSON_PINGS, resultSet.getInt(Constants.JSON_PINGS));
            playerData.put(Constants.JSON_RAPIERS, resultSet.getInt(Constants.JSON_RAPIERS));
            playerData.put(Constants.JSON_OBSERVERS, resultSet.getInt(Constants.JSON_OBSERVERS));
            playerData.put(Constants.JSON_SENTRIES, resultSet.getInt(Constants.JSON_SENTRIES));
            playerData.put(Constants.JSON_TOWER_DMG, resultSet.getLong(Constants.JSON_TOWER_DMG));
            playerData.put(Constants.JSON_HERO_DMG, resultSet.getLong(Constants.JSON_HERO_DMG));

            return playerData;
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);

            log.error("Error in getPlayerData method: {}", e.getMessage());
            return null;
        }
    }

    public static int deletePlayerData(String playerId) throws Exception {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "SqlDatabaseClient",
                "deletePlayerData()",
                "Delete player by ID",
                HistoryEntity.Status.SUCCESS,
                Constants.ACTOR_SYSTEM
        );

        String deleteQuery = Constants.SQL_PLAYER_DELETE;
        try (PreparedStatement stmt = getDatabase().prepareStatement(deleteQuery)) {
            stmt.setLong(1, Long.parseLong(playerId));
            int count = stmt.executeUpdate();
            if (count > 0)
                log.info("Player deleted successfully");
            else
                log.warn("No player found to delete");

            DatabaseUtil.save(historyEntity);
            return count;
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());
            DatabaseUtil.save(historyEntity);
            log.error("Error in deletePlayerData method: {}", e.getMessage());
            return -1;
        }
    }

    // TODO: players & pros played with
    // TODO: postgresql
    // TODO: interface
}
