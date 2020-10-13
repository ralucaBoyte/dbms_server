package dbms.service;


import dbms.domain.Attribute;
import dbms.domain.Database;
import dbms.domain.Index;
import dbms.domain.Table;
import dbms.dto.DatabaseTableDTO;
import dbms.repository.IRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;


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

    @Override
    public List<Database> getAllDatabases() {
        return repository.getAllDatabases();
    }

    public DatabaseTableDTO addTable(String databaseName, Table table){
        Table newTable = repository.addTable(databaseName, table);
        return new DatabaseTableDTO(databaseName, newTable);
    }

    @Override
    public Table removeTable(String databaseName, String tableName) {
        return repository.removeTable(databaseName, tableName);
    }

    @Override
    public Index addIndex(List<Attribute> attributeList, String databaseName, String tableName) {
        String indexName = attributeList.stream()
                .map(Attribute::getName)
                .collect(Collectors.joining(""));
        indexName += "_" + databaseName + "_" + tableName;
        Index indexToBeAdded = new Index(indexName, attributeList);
        return repository.addIndex(indexToBeAdded, databaseName, tableName);
    }
}
