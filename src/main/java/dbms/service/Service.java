package dbms.service;


import dbms.domain.*;
import dbms.dto.DatabaseTableDTO;
import dbms.dto.RecordMessageDTO;
import dbms.repository.IRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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
    public RecordMessageDTO addRecord(Record record, String databaseTableNames) {
        String[] parts = databaseTableNames.split("_");
        String databaseName = parts[0];
        String tableName = parts[1];

        RecordMessageDTO recordMessageDTO = new RecordMessageDTO();

        String fk = "";
        List<Attribute> attributeList = repository.findAllAttributesForDB_Table(databaseName, tableName);
        for(int i = 0; i < attributeList.size(); i++){
            if(attributeList.get(i).getForeignKey() != null){
                String tableNameFK = attributeList.get(i).getForeignKey().getKey().toString();

                List<Record> recordsForeignKey = findAllRecords(databaseName + "_" + tableNameFK);

                fk = record.getValue().split(";")[i-1];

                for(int j = 0 ; j < recordsForeignKey.size(); j++){
                    if(recordsForeignKey.get(j).getKey().equals(fk)){
                        repository.addRecord(record, databaseTableNames);
                        recordMessageDTO.setRecord(record);
                        return recordMessageDTO;
                    }
                }

            }
        }

        recordMessageDTO.setMessage("FK " + fk + " doesn't exist!");
        return recordMessageDTO;
    }

    @Override
    public List<Record> findAllRecords(String databaseTableNames) {
        Map<String, String> records = repository.findAllRecords(databaseTableNames);
        List<Record> recordList = new ArrayList<>();
        for (Map.Entry<String,String> record : records.entrySet()){
            recordList.add(new Record(record.getKey(), record.getValue()));
        }

        return recordList;
    }

    @Override
    public Record findRecordById(String id, String databaseTableNames) {
        String recordValue = repository.findRecordById(id, databaseTableNames);
        return new Record(id, recordValue);
    }

    @Override
    public Record deleteRecord(String id, String databaseTableNames) {
        Record recordToBeDeleted = new Record(id, repository.findRecordById(id, databaseTableNames));
        repository.deleteRecord(id, databaseTableNames);
        return recordToBeDeleted;
    }
}
