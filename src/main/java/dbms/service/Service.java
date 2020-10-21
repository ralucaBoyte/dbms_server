package dbms.service;


import dbms.domain.*;
import dbms.dto.DatabaseTableDTO;
import dbms.repository.IRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
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
    public Index addIndex(Index index, String databaseName, String tableName) {
        String fileName = index.getAttributeList().stream()
                .map(Attribute::getName)
                .collect(Collectors.joining(""));
        fileName += "_" + databaseName + "_" + tableName;
        index.setFilename(fileName);
        return repository.addIndex(index, databaseName, tableName);
    }

    @Override
    public void addRecord(Record record, String databaseTableNames) {
        repository.addRecord(record, databaseTableNames);
    }

    @Override
    public Map<String, String> findAllRecords(String databaseTableNames) {
        return repository.findAllRecords(databaseTableNames);
    }

    @Override
    public String findRecordById(String id, String databaseTableNames) {
        return repository.findRecordById(id, databaseTableNames);
    }

    @Override
    public void deleteRecord(String id, String databaseTableNames) {
        repository.deleteRecord(id, databaseTableNames);
    }
}
