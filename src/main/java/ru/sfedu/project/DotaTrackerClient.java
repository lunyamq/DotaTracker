package ru.sfedu.project;

import ru.sfedu.project.db.SqlDatabaseClient;
import ru.sfedu.project.utils.ConfigurationUtil;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class DotaTrackerClient {
    private static final Logger log = LogManager.getLogger(DotaTrackerClient.class);
    private static final String environment = System.getProperty(Constants.CONFIG_KEY, Constants.DEFAULT_CONFIG_PATH);
    private static final ConfigurationUtil util = new ConfigurationUtil(environment);

    public DotaTrackerClient() {
        log.debug("DotaTrackerClient: starting application...");
    }

    public static void logBasicSystemInfo() throws IOException {
        // create file if not exists and clean the file if exists
        File file = new File(Constants.DEFAULT_LOG_PATH);
        if (!file.exists())
            if (file.createNewFile())
                log.trace("created new log file");
            else
                log.trace("can't create new log file");
        else {
            FileWriter cleaner = new FileWriter(Constants.DEFAULT_LOG_PATH);
            cleaner.close();
        }

        log.info("Launching the application...");
        log.info("Operating System: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        log.info("JRE: {}", System.getProperty("java.version"));
        log.info("Java Launched From: {}", System.getProperty("java.home"));
        log.info("Class Path: {}", System.getProperty("java.class.path"));
        log.info("Library Path: {}", System.getProperty("java.library.path"));
        log.info("User Home Directory: {}", System.getProperty("user.home"));
        log.info("User Working Directory: {}", System.getProperty("user.dir"));
        log.info("Test INFO logging.");

        // java -Dconfig=C:\Users\lunqa\Documents\DotaTracker\src\main\resources\environment.xml -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar
        log.info("Environment: {}", util.getConfigurationEntry(Constants.PARAMS[3]));
    }




    public static void main(String[] args) throws Exception {
//        DotaTrackerClient.logBasicSystemInfo();
//
//        MongoDatabaseClient.putHeroes();
//        log.info(MongoDatabaseClient.getHero("Axe"));
//
//        MongoDatabaseClient.putPatches();
//        log.info(MongoDatabaseClient.getPatch("7.32"));

//        SqlDatabaseClient.putMatch("8087422283");
//        System.out.println(SqlDatabaseClient.getMatchHeroes("8087422283"));

//        SqlDatabaseClient.putMatchHeroes("8084063499");
//        System.out.println(SqlDatabaseClient.getMatch("8084063499"));
//        SqlDatabaseClient.deleteMatch("8084063499");

        System.out.println(SqlDatabaseClient.getMatchData("8088747175"));

        // ПЕРЕД ПОЛУЧЕНИЕМ ПОБОЧНЫХ ТАБЛИЦ - PUTMATCH()

//        SqlDatabaseClient.putPlayer("321580662");

        log.info("end");
    }
}
