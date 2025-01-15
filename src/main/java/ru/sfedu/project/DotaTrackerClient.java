package ru.sfedu.project;

import static ru.sfedu.project.Constants.log;
import static ru.sfedu.project.Constants.util;

import picocli.CommandLine;
import ru.sfedu.project.db.SqlDatabaseClient;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;


public class DotaTrackerClient {
    public DotaTrackerClient() {
        log.debug("DotaTrackerClient: starting application...");
    }

    public static void logBasicSystemInfo() throws IOException {
        // create file if not exists and clean the file if exists
        File file = new File(Constants.DEFAULT_LOG_PATH);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            if (parentDir.mkdirs())
                log.debug("Created directories for log file");
            else {
                log.error("Failed to create directories for log file");
                throw new RuntimeException("Failed to create directories for log file");

            }
        }

        if (!file.exists())
            if (file.createNewFile())
                log.debug("created new log file");
            else {
                log.error("can't create new log file");
                throw new RuntimeException("can't create new log file");
            }
        else {
            FileWriter cleaner = new FileWriter(Constants.DEFAULT_LOG_PATH);
            cleaner.close();
        }

        log.debug("Launching the application...");
        log.debug("Operating System: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        log.debug("JRE: {}", System.getProperty("java.version"));
        log.debug("Java Launched From: {}", System.getProperty("java.home"));
        log.debug("Class Path: {}", System.getProperty("java.class.path"));
        log.debug("Library Path: {}", System.getProperty("java.library.path"));
        log.debug("User Home Directory: {}", System.getProperty("user.home"));
        log.debug("User Working Directory: {}", System.getProperty("user.dir"));
        log.debug("Test INFO logging.");

        // java -Dconfig=C:\Users\lunqa\Documents\DotaTracker\src\main\resources\environment.xml -jar .\target\DotaTracker-1.0-jar-with-dependencies.jar
        log.debug("Environment: {}", util.getConfigurationEntry(Constants.PARAMS[3]));
    }

    public static void main(String[] args) throws Exception {
        logBasicSystemInfo();

        int exitCode = new CommandLine(new DotaCLI()).execute(args);
        System.exit(exitCode);
    }
}
