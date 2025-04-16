package ru.sfedu.project;

import static ru.sfedu.project.Constants.log;
import static ru.sfedu.project.Constants.util;

import picocli.CommandLine;
import ru.sfedu.project.db.HibernateMysqlClient;
import ru.sfedu.project.entities.CustomComponent;
import ru.sfedu.project.entities.TestEntity;
import ru.sfedu.project.utils.HibernateUtil;
import jakarta.persistence.Tuple;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;


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

        log.debug("Environment: {}", util.getConfigurationEntry(Constants.PARAMS[3]));
    }

    public static void main(String[] args) throws Exception {
        logBasicSystemInfo();

        log.info(HibernateUtil.getUsers());
        log.info(HibernateUtil.getDatabaseSize());
        log.info(HibernateUtil.getAllTables());
        List<Tuple> colMatrix = HibernateUtil.getColumnTypes();
        for (Tuple row : colMatrix) {
            String tableName = row.get(0, String.class);
            String columnName = row.get(1, String.class);
            String columnType = row.get(2, String.class);

            log.info("Table: {}, Column: {}, Type: {}", tableName, columnName, columnType);
        }

        TestEntity entity = new TestEntity();
        entity.setName("Test Object");
        entity.setDescription("This is a test description");
        entity.setDateCreated(new Date());
        entity.setCheck(true);

        CustomComponent component = new CustomComponent();
        component.setField1("Test Field 1");
        component.setField2(123);
        entity.setCustomComponent(component);

        Long id = HibernateMysqlClient.createTestEntity(entity);

        TestEntity readEntity = HibernateMysqlClient.readTestEntity(id);
        log.info("Прочитана сущность: ID={}, Name={}, Description={}, Check={}",
            readEntity.getId(),
            readEntity.getName(),
            readEntity.getDescription(),
            readEntity.getCheck()
        );

        readEntity.setName("Updated Name");
        readEntity.setDescription("Updated Description");
        readEntity.setCheck(false);
        HibernateMysqlClient.updateTestEntity(readEntity);
        TestEntity updatedEntity = HibernateMysqlClient.readTestEntity(id);
        log.info("Проверка обновления: новое имя - {}", updatedEntity.getName());

//        HibernateMysqlClient.deleteTestEntity(id);
//        log.info("Сущность с ID {} удалена", id);

        int exitCode = new CommandLine(new DotaCLI()).execute(args);
        System.exit(exitCode);
    }
}
