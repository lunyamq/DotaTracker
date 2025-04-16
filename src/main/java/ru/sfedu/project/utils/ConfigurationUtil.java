package ru.sfedu.project.utils;

import static ru.sfedu.project.Constants.log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Configuration utility. Allows to get configuration properties from the
 * default configuration file
 *
 * @author Boris Jmailov
 */
public class ConfigurationUtil {
    private static final Properties configuration = new Properties();
//    private static File nf;

    protected static String getFileExtension(File file) {
        String filename = file.getName();
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
    protected static String getFileExtension(String file) {
        return file.substring(file.lastIndexOf('.') + 1).toLowerCase();
    }
    /**
     * Hides default constructor
     */
    public ConfigurationUtil(String path) {
//        nf = new File(path);
//
//        if (!nf.exists() || !nf.isFile())
//            throw new IllegalArgumentException("Configuration file not found: " + path);


        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null)
                throw new IllegalArgumentException("Configuration file not found: " + path);

            String fileExtension = getFileExtension(path);
            switch (fileExtension) {
                case "properties":
                case "yml":
                case "yaml":
                    configuration.load(inputStream);
                    break;
                case "xml":
                    configuration.loadFromXML(inputStream);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported file type: " + fileExtension);
            }
        } catch (Exception e) {
            log.error("Error loading configuration");
            throw new RuntimeException("Error loading configuration from: " + path, e);
        }
    }

    /**
     * Gets configuration entry value
     * @param key Entry key
     * @return Entry value by key
     */
    public String getConfigurationEntry(String key) {
        return configuration.getProperty(key);
    }
}
