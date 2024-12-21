package ru.sfedu.project;


public class Constants {
    public static final String API = "https://api.opendota.com/api/";

    public static final String DEFAULT_LOG_PATH = ".\\logs\\application.log";
    public static final String CSV_LOG_PATH = ".\\logs\\log.csv";
    public static final String XML_LOG_PATH = ".\\logs\\log.xml";
    public static final String DEFAULT_CONFIG_PATH = ".\\src\\main\\resources\\environment.properties";
    public static final String XML_CONFIG_PATH = ".\\src\\main\\resources\\environment.xml";
    public static final String YML_CONFIG_PATH = ".\\src\\main\\resources\\environment.yml";

    public static final String CONFIG_KEY = "config";
    public static final String[] PARAMS = { "app.name", "app.version", "app.author", "env.type",
            "mongo.connect.local", "mongo.connect.cluster", "sql.connect", "sql.user", "sql.password" };

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
}
