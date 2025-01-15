package ru.sfedu.project.utils;

import java.io.File;
import java.io.FileInputStream;
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
    private static File nf;

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

            configuration.load(inputStream);
        } catch (Exception e) {

            throw new RuntimeException("Error loading configuration from: " + path, e);
        }
    }

    private static Properties getConfiguration() throws IOException {
        if(configuration.isEmpty()) loadConfiguration();

        return configuration;
    }

    protected static String getFileExtension(File file) {
        String filename = file.getName();
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Loads configuration from <code>DEFAULT_CONFIG_PATH</code>
     * @throws IOException In case of the configuration file read failure
     */
    private static void loadConfiguration() throws IOException {
//         DEFAULT_CONFIG_PATH.getClass().getResourceAsStream(DEFAULT_CONFIG_PATH);

        try (InputStream in = new FileInputStream(nf)) {
            String fileExtension = getFileExtension(nf);
            switch (fileExtension) {
                case "properties":
                case "yml":
                case "yaml":
                    configuration.load(in);
                    break;
                case "xml":
                    configuration.loadFromXML(in);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported file type: " + fileExtension);
            }
        } catch (IOException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Gets configuration entry value
     * @param key Entry key
     * @return Entry value by key
     * @throws IOException In case of the configuration file read failure
     */
    public String getConfigurationEntry(String key) throws IOException {
        return getConfiguration().getProperty(key);
    }
}
