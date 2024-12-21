package ru.sfedu.project.interfaces;

public interface IDataProvider<T> {
    void initDataSource(String recordPath) throws Exception;
    T getRecordById(String id);
    void saveRecord(T record) throws Exception;
    void deleteRecord(T record) throws Exception;
}
