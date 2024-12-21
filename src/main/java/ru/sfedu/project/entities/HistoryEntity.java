package ru.sfedu.project.entities;

import org.bson.types.ObjectId;
import org.simpleframework.xml.Transient;
import ru.sfedu.project.HistoryContent;
import java.time.Instant;
import java.util.Date;

public class HistoryEntity {
    @Transient
    private ObjectId id;
    private Date creationDate;
    private String className;
    private String methodName;
    private Object message;
    private Status status;
    private String actor;


    public enum Status {
        SUCCESS,
        FAIL
    }

    public HistoryEntity() { }

    public HistoryEntity(String id) {
        this.id = new ObjectId(id);
    }

    public HistoryEntity(Date creationDate, String className, String methodName, Object message, Status status, String actor) {
        this.creationDate = creationDate;
        this.className = className;
        this.methodName = methodName;
        this.message = message;
        this.status = status;
        this.actor = actor;
    }

    public HistoryEntity(ObjectId id, Date creationDate, String className, String methodName, Object message, Status status, String actor) {
        this.id = id;
        this.creationDate = creationDate;
        this.className = className;
        this.methodName = methodName;
        this.message = message;
        this.status = status;
        this.actor = actor;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public static String getInfo(HistoryEntity entity) {
        if (entity == null) return null;

        return "id: " + entity.getId() +
                ", creationDate: " + entity.getCreationDate() +
                ", className: " + entity.getClassName() +
                ", methodName: " + entity.getMethodName() +
                ", message: " + entity.getMessage() +
                ", status: " + entity.getStatus() +
                ", actor: " + entity.getActor();
    }

    public String[] toCsv() {
        return new String[] {
                String.valueOf(HistoryContent.getId()),
                String.valueOf(getCreationDate().toInstant()),
                getClassName(),
                getMethodName(),
                String.valueOf(getMessage()),
                String.valueOf(getStatus()),
                getActor()
        };
    }

    public static HistoryEntity toClass(String[] csv) {
        Instant instant = Instant.parse(csv[1]);
        Date date = Date.from(instant);

        return new HistoryEntity(
                new ObjectId(csv[0]),
                date,
                csv[2],
                csv[3],
                csv[4],
                Status.valueOf(csv[5]),
                csv[6]
        );
    }
}
