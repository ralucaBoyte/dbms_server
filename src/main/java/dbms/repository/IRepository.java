package dbms.repository;

import dbms.domain.Database;
import dbms.domain.Index;
import dbms.domain.Table;

import java.util.List;
import java.util.Optional;

public interface IRepository {
    Database addDatabase(Database database);
    Database removeDatabase(String databaseName);
    List<Database> getAllDatabases();
    Table addTable(String databaseName, Table table);
    Table removeTable(String databaseName, String tableName);
    Table getTableByDatabaseName(String databaseName,String tableName);
    Index addIndex(Index index, String databaseName, String tableName);
}
