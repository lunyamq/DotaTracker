package ru.sfedu.project.utils;

import static ru.sfedu.project.Constants.log;

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

    public XmlWrapper() {}

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

    private void save()  {
        try {
            Serializer serializer = new Persister();
            serializer.write(new XmlWrapper(records), new File(recordPath));
        } catch (Exception e) {
            log.debug("Error in save method: {}", e.getMessage());
        }
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

    public HistoryEntity getRecord(String methodName) {
        return records.stream()
                .filter(rec -> Objects.equals(rec.getMethodName(), methodName))
                .findFirst()
                .orElse(new HistoryEntity());
    }

    @Override
    public void saveRecord(HistoryEntity record) {
        records.add(record);
        save();
    }
}
