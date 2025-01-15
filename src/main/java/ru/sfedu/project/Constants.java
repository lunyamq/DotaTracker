package ru.sfedu.project;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.project.db.MongoDatabaseClient;
import ru.sfedu.project.utils.ConfigurationUtil;

public class Constants {
    public static final String API = "https://api.opendota.com/api/";

    public static final String environment = System.getProperty(Constants.CONFIG_KEY, Constants.DEFAULT_CONFIG_PATH);
    public static final ConfigurationUtil util = new ConfigurationUtil(environment);
    public static final Logger log = LogManager.getLogger(Constants.class);

    public static final String DEFAULT_LOG_PATH = ".\\logs\\application.log";
    public static final String CSV_LOG_PATH = ".\\logs\\log.csv";
    public static final String XML_LOG_PATH = ".\\logs\\log.xml";
    public static final String DEFAULT_CONFIG_PATH = "environment.properties";
    public static final String TEST_CONFIG_PATH = "env.properties";
    public static final String XML_CONFIG_PATH = "environment.xml";
    public static final String YML_CONFIG_PATH = "environment.yml";

    public static final String CONFIG_KEY = "config";
    public static final String[] PARAMS = { "app.name", "app.version", "app.author", "env.type",
            "mongo.connect.local", "mongo.connect.cluster", "sql.connect", "sql.user", "sql.password" };

    public static final String[] RANK_NAMES = {
            "Herald", "Guardian", "Crusader", "Archon", "Legend", "Ancient", "Divine", "Immortal"
    };

    public static final String MONGO_DB_NAME = "dotaTracker";
    public static final String[] MONGO_DB_COLLECTIONS = { "Logs", "Heroes", "Patches" };
    public static final String MONGO_DB_ID = "_id";
    public static final String[] MONGO_DB_LOGS_FIELDS = {
            "creationDate",
            "className",
            "methodName",
            "message",
            "status",
            "actor"
    };
    public static final String[] MONGO_DB_HEROES_FIELDS = { "localized_name", "data" };
    public static final String[] MONGO_DB_PATCHES_FIELDS = { "id", "name", "date" };


    public static final String ACTOR_SYSTEM = "system";
    public static final String ACTOR_USER = "user";

    public static final String API_CONSTANTS_HEROES = "constants/heroes";
    public static final String API_CONSTANTS_PATCHES = "constants/patch";

    public static final String API_CONSTANTS_MATCHES = "matches/";
    public static final String API_CONSTANTS_PLAYERS = "players/";
    public static final String API_CONSTANTS_PLAYERS_WL = "/wl";
    public static final String API_CONSTANTS_PLAYERS_SUM = "/totals";

    public static final String JSON_ID = "id";
    public static final String JSON_ACCOUNT_NAME = "name";
    public static final String JSON_RANG = "rang";
    public static final String JSON_WINS = "wins";
    public static final String JSON_LOSES = "losses";
    public static final String JSON_HAS_PLUS = "hasPlus";
    public static final String JSON_PINGS = "pings";
    public static final String JSON_RAPIERS = "rapiers";
    public static final String JSON_OBSERVERS = "observers";
    public static final String JSON_SENTRIES = "sentries";
    public static final String JSON_TOWER_DMG = "towerDmg";
    public static final String JSON_HERO_DMG = "heroDmg";

    public static final String JSON_PLAYER = "players";
    public static final String JSON_ACCOUNT = "account_id";
    public static final String JSON_NAME = "personaname";
    public static final String JSON_HERO = "hero_id";
    public static final String JSON_TEAM = "isRadiant";
    public static final String[] JSON_KDA = { "kills", "deaths", "assists" };
    public static final String JSON_RANK = "rank_tier";
    public static final String JSON_WIN = "win";
    public static final String JSON_LOSE = "lose";
    public static final String JSON_PLUS = "plus";
    public static final String JSON_FIELD = "field";
    public static final String JSON_SUM = "sum";
    public static final String JSON_PLAYER_ID = "player_id";
    public static final String JSON_PLAYER_NAME = "player_name";
    public static final String JSON_HERO_NAME = "hero_name";
    public static final String JSON_IS_RADIANT = "is_radiant";

    public static final String JSON_MATCH = "match_id";
    public static final String JSON_DURATION = "duration";
    public static final String JSON_FB = "first_blood_time";
    public static final String[] JSON_SCORE = { "radiant_score", "dire_score" };
    public static final String JSON_WINNER = "radiant_win";
    public static final String JSON_CHAT = "chat";
    public static final String JSON_REPLAY = "replay_url";
    public static final String JSON_START = "start_time";
    public static final String JSON_FIRST_BLOOD = "firstBlood";
    public static final String[] JSON_TEAM_SCORE = { "radiantScore", "direScore" };
    public static final String JSON_TEAM_WINNER = "radiantWin";
    public static final String JSON_DEMO_LINK = "demoLink";
    public static final String JSON_MATCH_DATE = "matchDate";

    public static final String SQL_MATCH_PLAYERS_SELECT = "SELECT * FROM match_players WHERE match_id = ?";
    public static final String SQL_MATCH_SELECT = "SELECT * FROM matches WHERE id = ?";
    public static final String SQL_PLAYER_SELECT = "SELECT * FROM players WHERE id = ?";

    public static final String SQL_MATCH_DELETE = "DELETE FROM matches WHERE id = ?";
    public static final String SQL_PLAYER_DELETE = "DELETE FROM players WHERE id = ?";

    public static final String SQL_MATCHES_TABLE = """
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
    public static final String SQL_PLAYERS_TABLE = """
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
    public static final String SQL_MATCH_PLAYERS_TABLE = """
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
                FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE
            );
    """;

    public static final String SQL_MATCH_PLAYERS_INSERT = """
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

    public static final String SQL_MATCHES_INSERT = """
            INSERT INTO matches (
                id, duration, firstBlood, radiantScore, direScore, radiantWin, chat, demoLink, matchDate
            )
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

    public static final String SQL_PLAYERS_INSERT = """
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
}
