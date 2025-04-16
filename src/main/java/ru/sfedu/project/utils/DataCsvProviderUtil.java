package ru.sfedu.project.utils;

import ru.sfedu.project.entities.HistoryEntity;
import ru.sfedu.project.interfaces.IDataProvider;
import static ru.sfedu.project.Constants.log;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.bson.types.ObjectId;
import java.io.*;
import java.util.List;
import java.util.Objects;

public class DataCsvProviderUtil implements IDataProvider<HistoryEntity> {
    private String recordPath;

    @Override
    public void initDataSource(String recordPath) throws IOException, IllegalArgumentException {
        this.recordPath = recordPath;

        // create file if not exists
        File csv = new File(this.recordPath);
        if (!ConfigurationUtil.getFileExtension(csv).equals("csv")) {
            log.error("Error in initDataSource method");
            throw new IllegalArgumentException();
        }

        if (!csv.exists())
            if (csv.createNewFile())
                log.info("csv file created");
    }

    public HistoryEntity getRecordById(String id) {
        try (CSVReader reader = new CSVReader(new FileReader(recordPath))) {
            List<String[]> records = reader.readAll();
            for (String[] rec : records) {
                if (Objects.equals(rec[0], id))
                    return HistoryEntity.toClass(rec);
            }
        } catch (Exception e) {
            log.error("Error in getRecordById method: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return new HistoryEntity();
    }

    public HistoryEntity getRecordById(ObjectId id) {
        return getRecordById(String.valueOf(id));
    }

    @Override
    public void saveRecord(HistoryEntity record) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(recordPath, true))) {
            writer.writeNext(record.toCsv());
        } catch (Exception e) {
            log.error("Error in saveRecord method: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteRecord(HistoryEntity record) {
        List<String[]> records;

        try (CSVReader reader = new CSVReader(new FileReader(recordPath))) {
            records = reader.readAll();
        } catch (Exception e) {
            log.error("Error in deleteRecord(read) method: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(recordPath))) {
            for (String[] rec : records) {
                if (!rec[0].equals(String.valueOf(record.getId())))
                    writer.writeNext(rec);
            }
        } catch (Exception e) {
            log.error("Error in deleteRecord(write) method: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteRecordById(String id) {
        HistoryEntity entity = new HistoryEntity(id);
        deleteRecord(entity);
    }
}
