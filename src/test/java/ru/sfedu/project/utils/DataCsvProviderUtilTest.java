package ru.sfedu.project.utils;

import org.junit.jupiter.api.Test;
import ru.sfedu.project.Constants;
import ru.sfedu.project.HistoryContent;
import ru.sfedu.project.entities.HistoryEntity;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DataCsvProviderUtilTest {

    @Test
    void record() throws IOException {
        HistoryEntity entity = new HistoryEntity(
                new Date(),
                "DataXmlProviderUtilTest",
                "test()",
                null,
                HistoryEntity.Status.SUCCESS,
                null
        );

        DataCsvProviderUtil util = new DataCsvProviderUtil();
        util.initDataSource(Constants.CSV_LOG_PATH);
        HistoryContent.saveHistory(entity);
        util.saveRecord(entity);

        HistoryEntity findEntity = util.getRecordById(entity.getId());
        String log = HistoryEntity.getInfo(findEntity);
        assertNotNull(log, "getRecordById error!");

        util.deleteRecord(entity);
        findEntity = util.getRecordById(entity.getId());
        assertTrue(findEntity.isEmpty(), "deleteRecord error!");
    }
}