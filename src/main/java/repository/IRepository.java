package repository;

import domain.Database;
import domain.Table;

public interface IRepository {
    Database addDatabase(Database database);
    Table addTable(String databaseName, Table table);
    Database removeDatabase(String databaseName);
    Table removeTable(String tableName);
}
