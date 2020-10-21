package dbms.service;

import dbms.domain.*;
import dbms.dto.DatabaseTableDTO;

import java.util.List;
import java.util.Map;

public interface IService {
    Database addDatabase(Database database);
    Database removeDatabase(String databaseName);
    List<Database> getAllDatabases();
    DatabaseTableDTO addTable(String databaseName, Table table);
    Table removeTable(String databaseName, String tableName);
    Index addIndex(Index index, String databaseName, String tableName);

    void addRecord(Record record, String databaseTableNames);
    Map<String, String> findAllRecords(String databaseTableNames);
    String findRecordById(String id, String databaseTableNames);
    void deleteRecord(String id, String databaseTableNames);
}
