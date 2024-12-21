package ru.sfedu.project;

import java.io.IOException;
import java.util.*;
import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DotaTrackerClientTest {

    @Test
    void logBasicSystemInfo() throws IOException {
        DotaTrackerClient.logBasicSystemInfo(); // log

        File file = new File(Constants.DEFAULT_LOG_PATH);
        Scanner scanner = new Scanner(file);

        String[] tests = new String[] {
                "Launching the application...", "Operating System:", "JRE", "Java Launched From:",
                "Class Path:", "Library Path:", "User Home Directory:", "User Working Directory:", "Test INFO logging."
        };
        String[] errors = new String[] {
                "Launch error!", "OS error!", "JRE error!", "Java error!",
                "Class error!", "Lib error!", "Home dir error!", "Work dir error!", "End error!"
        };

        for (int i = 0; i < tests.length; i++) {
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Assertions.assertTrue(line.contains(tests[i]), errors[i]);
            }
            else Assertions.fail(errors[i]);
        }

        // mvn test -Dconfig=C:\Users\lunqa\Documents\DotaTracker\src\main\resources\environment.xml
        String environment = System.getProperty(Constants.CONFIG_KEY, Constants.DEFAULT_CONFIG_PATH);
        if (environment.contains(Constants.DEFAULT_CONFIG_PATH.substring(1)))
            Assertions.assertTrue(scanner.nextLine().contains("Property"), "Environment error!");
        else if (environment.contains(Constants.XML_CONFIG_PATH.substring(1)))
            Assertions.assertTrue(scanner.nextLine().contains("XML"), "Environment error!");
        else if (environment.contains(Constants.YML_CONFIG_PATH.substring(1)))
            Assertions.assertTrue(scanner.nextLine().contains("YML"), "Environment error!");
        else
            Assertions.fail("Environment path error!");

        if (scanner.hasNextLine()) Assertions.fail("Trash info!");
        scanner.close();
    }
}