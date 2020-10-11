package dbms.service;

import dbms.domain.Database;
import dbms.domain.Table;
import dbms.dto.DatabaseTableDTO;

public interface IService {
    Database addDatabase(Database database);
    DatabaseTableDTO addTable(String databaseName, Table table);
}
