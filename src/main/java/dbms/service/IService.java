package dbms.service;

import dbms.domain.Database;
import dbms.domain.Table;
import dbms.dto.DatabaseTableDTO;

import java.util.List;

public interface IService {
    Database addDatabase(Database database);
    Database removeDatabase(String databaseName);
    List<Database> getAllDatabases();
    DatabaseTableDTO addTable(String databaseName, Table table);
    Table removeTable(String databaseName, String tableName);
}
