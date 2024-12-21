package ru.sfedu.project.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.sfedu.project.Constants;
import ru.sfedu.project.entities.HistoryEntity;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

class DataXmlProviderUtilTest {

    @Test
    void saveRecord() throws Exception {
        // create file if not exists and clean the file if exists
        File xml = new File(Constants.XML_LOG_PATH);
        if (!xml.exists())
            xml.createNewFile();
        else {
            FileWriter cleaner = new FileWriter(Constants.XML_LOG_PATH);
            cleaner.close();
        }

        HistoryEntity entity = new HistoryEntity(
                new Date(),
                "DataCsvProviderUtilTest",
                "test()",
                null,
                HistoryEntity.Status.FAIL,
                null
        );

        DataXmlProviderUtil util = new DataXmlProviderUtil();
        util.initDataSource(Constants.XML_LOG_PATH);
        util.saveRecord(entity);

        HistoryEntity result = util.getRecord("test()");
        Assertions.assertEquals(result.getCreationDate(), entity.getCreationDate(), "XML write error!");
    }
}