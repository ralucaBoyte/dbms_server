package dbms.repository;

import dbms.domain.*;

import java.util.List;
import java.util.Map;

public interface IRepository {
    Database addDatabase(Database database);
    Database removeDatabase(String databaseName);
    List<Database> getAllDatabases();
    Table addTable(String databaseName, Table table);
    Table removeTable(String databaseName, String tableName);
    Table getTableByDatabaseName(String databaseName,String tableName);
    Index addIndex(Index index, String databaseName, String tableName);
    List<Attribute> findAllAttributesForDB_Table(String databaseName, String tableName);
    void deleteAllRecordsFromTable(String databaseTableName);
    List<Table> getAllTables(String database);
    List<Index> getAllIndexesForDBandTable(String databaseName, String tableName);

    void addRecord(Record record, String databaseTableNames);
    Map<String, String> findAllRecords(String databaseTableNames);
    String findRecordById(String id, String databaseTableNames);
    void deleteRecord(String id, String databaseTableNames);
    List<Pair> hasForeignKey(Table tableName);

}
