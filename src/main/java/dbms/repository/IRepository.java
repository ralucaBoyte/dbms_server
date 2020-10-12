package dbms.repository;

import dbms.domain.Database;
import dbms.domain.Index;
import dbms.domain.Table;

import java.util.List;

public interface IRepository {
    Database addDatabase(Database database);
    Database removeDatabase(String databaseName);
    List<Database> getAllDatabases();
    Table addTable(String databaseName, Table table);
    Table removeTable(String databaseName, String tableName);
    Index addIndex(Index index);
}
