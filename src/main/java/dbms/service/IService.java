package dbms.service;

import dbms.domain.Database;
import dbms.domain.Table;
import dbms.dto.DatabaseTableDTO;

public interface IService {
    Database addDatabase(Database database);
    Database removeDatabase(String databaseName);
    DatabaseTableDTO addTable(String databaseName, Table table);
    Table removeTable(String databaseName, String tableName);
}
