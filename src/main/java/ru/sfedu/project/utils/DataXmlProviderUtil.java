package ru.sfedu.project.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.sfedu.project.entities.HistoryEntity;
import ru.sfedu.project.interfaces.IDataProvider;
import java.io.File;
import org.simpleframework.xml.ElementList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class XmlWrapper {
    @ElementList
    private List<HistoryEntity> records;

    public XmlWrapper() {};

    public XmlWrapper(List<HistoryEntity> records) {
        this.records = records;
    }

    public List<HistoryEntity> getRecords() {
        return records;
    }
}


public class DataXmlProviderUtil implements IDataProvider<HistoryEntity> {
    private String recordPath;
    private List<HistoryEntity> records = new ArrayList<>();
    private static final Logger log = LogManager.getLogger(DataXmlProviderUtil.class);

    private void save() throws Exception {
        Serializer serializer = new Persister();
        serializer.write(new XmlWrapper(records), new File(recordPath));
    }

    @Override
    public void initDataSource(String recordPath) throws Exception {
        this.recordPath = recordPath;

        File xml = new File(recordPath);
        if (!ConfigurationUtil.getFileExtension(xml).equals("xml")) {
            log.error("Error in initDataSource method");
            throw new IllegalArgumentException();
        }

        if (!xml.exists()) {
            if (xml.createNewFile())
                log.info("xml file created");
            save();
        } else if (xml.length() == 0)
            save();
        else {
            Serializer serializer = new Persister();
            records = serializer.read(XmlWrapper.class, xml).getRecords();
        }
    }

    @Override
    public HistoryEntity getRecordById(String id) {
        return null;
    }

    public HistoryEntity getRecordById(ObjectId id) {
        return null;
    }

    public HistoryEntity getRecord(String methodName) {
        return records.stream()
                .filter(rec -> Objects.equals(rec.getMethodName(), methodName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void saveRecord(HistoryEntity record) throws Exception {
        records.add(record);
        save();
    }

    @Override
    public void deleteRecord(HistoryEntity record) throws Exception { }

    public void deleteRecordById(String id) throws Exception { }
}
