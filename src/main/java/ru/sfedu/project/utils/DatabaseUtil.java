package ru.sfedu.project.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import ru.sfedu.project.Constants;
import ru.sfedu.project.HistoryContent;
import ru.sfedu.project.entities.HistoryEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class DatabaseUtil {
    private static final Logger log = LogManager.getLogger(DatabaseUtil.class);
    private static final DataCsvProviderUtil csvUtil = new DataCsvProviderUtil();
    private static final DataXmlProviderUtil xmlUtil = new DataXmlProviderUtil();

    static {
        try {
            csvUtil.initDataSource(Constants.CSV_LOG_PATH);
            xmlUtil.initDataSource(Constants.XML_LOG_PATH);
        } catch (Exception e) {
            log.error("Error in static: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static JSONObject getJsonObj(String link) throws IOException {
        URI uri = URI.create(Constants.API + link);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200)
            return new JSONObject();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return new JSONObject(new JSONTokener(in));
    }

    public static JSONArray getJsonArr(String link) throws IOException {
        URI uri = URI.create(Constants.API + link);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200)
            return new JSONArray();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return new JSONArray(new JSONTokener(in));
    }

    public static void save(HistoryEntity entity) throws Exception {
        HistoryContent.saveHistory(entity);
        csvUtil.saveRecord(entity);
        xmlUtil.saveRecord(entity);
    }
}
