package ru.sfedu.project.interfaces;

public interface IDataProvider<T> {
    void initDataSource(String recordPath) throws Exception;
    void saveRecord(T record) throws Exception;
}
