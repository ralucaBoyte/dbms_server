package dbms.repository;

import dbms.domain.Database;
import dbms.domain.Table;

public interface IRepository {
    Database addDatabase(Database database);
    Table addTable(String databaseName, Table table);
    Database removeDatabase(String databaseName);
    Table removeTable(String databaseName, String tableName);
}
