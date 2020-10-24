package dbms.service;

import dbms.domain.*;
import dbms.dto.DatabaseTableDTO;
import dbms.dto.RecordMessageDTO;

import java.util.List;
import java.util.Map;

public interface IService {
    Database addDatabase(Database database);
    Database removeDatabase(String databaseName);
    List<Database> getAllDatabases();
    DatabaseTableDTO addTable(String databaseName, Table table);
    Table removeTable(String databaseName, String tableName);
    Index addIndex(Index index, String databaseName, String tableName);

    RecordMessageDTO addRecord(Record record, String databaseTableNames);
    List<Record> findAllRecords(String databaseTableNames);
    Record findRecordById(String id, String databaseTableNames);
    Record deleteRecord(String id, String databaseTableNames);
}
