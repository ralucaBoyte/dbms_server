package dbms.service;


import dbms.domain.Database;
import dbms.domain.Table;
import dbms.dto.DatabaseTableDTO;
import dbms.repository.IRepository;
import org.springframework.beans.factory.annotation.Autowired;


public class Service implements IService{
    @Autowired
    private IRepository repository;

    public Service(){};

    public Database addDatabase(Database database){
        return repository.addDatabase(database);
    }

    @Override
    public Database removeDatabase(String databaseName) {
        return repository.removeDatabase(databaseName);
    }

    public DatabaseTableDTO addTable(String databaseName, Table table){
        Table newTable = repository.addTable(databaseName, table);
        return new DatabaseTableDTO(databaseName, newTable);
    }

    @Override
    public Table removeTable(String databaseName, String tableName) {
        return repository.removeTable(databaseName, tableName);
    }
}
