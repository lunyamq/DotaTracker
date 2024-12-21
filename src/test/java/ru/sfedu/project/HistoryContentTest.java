package ru.sfedu.project;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import ru.sfedu.project.entities.HistoryEntity;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class HistoryContentTest {

    @Test
    void saveHistory() throws IOException {
        HistoryEntity historyEntity = new HistoryEntity(
                new Date(),
                "TestClass",
                "testMethod",
                "Test Object",
                HistoryEntity.Status.SUCCESS,
                "testActor"
        );

        HistoryContent.saveHistory(historyEntity);

        ObjectId historyId = HistoryContent.getId();
        HistoryEntity savedHistory = HistoryContent.getHistory(historyId);

        assertNotNull(savedHistory, "Null Entity error!");
        assertEquals("TestClass", savedHistory.getClassName(), "ClassName error!");
        assertEquals("testMethod", savedHistory.getMethodName(), "MethodName error!");
        assertEquals("Test Object", savedHistory.getMessage(), "Object error!");
        assertEquals(HistoryEntity.Status.SUCCESS, savedHistory.getStatus(), "Status error!");
        assertEquals("testActor", savedHistory.getActor(), "Actor error!");
    }
}