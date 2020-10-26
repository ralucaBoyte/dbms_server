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
        boolean existsForeignKeyAttribute = false;

        List<Attribute> attributeList = repository.findAllAttributesForDB_Table(databaseName, tableName);
        for(int i = 0; i < attributeList.size(); i++){
            if(attributeList.get(i).getForeignKey() != null){
                existsForeignKeyAttribute = true;
                String tableNameFK = attributeList.get(i).getForeignKey().getKey().toString();
                List<Record> recordsForeignKey = findAllRecords(databaseName + "_" + tableNameFK);
                fk = record.getValue().split(";")[i-1];

                for (Record value : recordsForeignKey) {
                    if (value.getKey().equals(fk)) {
                        repository.addRecord(record, databaseTableNames);
                        recordMessageDTO.setRecord(record);
                        return recordMessageDTO;
                    }
                }

            }
        }

        if(existsForeignKeyAttribute){
            recordMessageDTO.setMessage("FK " + fk + " doesn't not refer to an existing element!");
            return recordMessageDTO;
        }
        else {
            //Check if primary key is unique
            Map<String, String> primaryKeys = repository.findAllRecords(databaseTableNames);
            if(primaryKeys.containsKey(record.getKey())){
                recordMessageDTO.setMessage("Primary key must be unique");
                return recordMessageDTO;
            }
            recordMessageDTO.setRecord(record);
            repository.addRecord(record, databaseTableNames);
            return recordMessageDTO;
        }



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
    public RecordMessageDTO deleteRecord(String id, String databaseTableNames) {

        String[] parts = databaseTableNames.split("_");
        String databaseName = parts[0];
        String tableName = parts[1];

        Record recordToBeDeleted = new Record(id, repository.findRecordById(id, databaseTableNames));
        RecordMessageDTO recordToBeDeletedDTO = new RecordMessageDTO();

        //GET ALL TABLES FROM DATABASE databaseName, EXCEPT tableName
        List<Table> tableList = repository.getAllTables(databaseName).stream().filter(t->!t.getName().equals(tableName)).collect(Collectors.toList());

        //ITERATE THROUGH TABLES TO SEE IF EXISTS ONE TABLE WITH A FK REFERENCING tableName TABLE
        for(Table table: tableList){

            //GET ALL FOREIGN KEYS FOR CURRENT TABLE AND CHECK IF EXISTS ONE WHICH REFERS TO tableName TABLE
            List<Attribute> attributeList = repository.findAllAttributesForDB_Table(databaseName, table.getName());
            for(int i = 0; i < attributeList.size(); i ++){
                if(attributeList.get(i).getForeignKey() != null && attributeList.get(i).getForeignKey().getKey().toString().equals(tableName)){
                    Map<String, String> recordsList = repository.findAllRecords(databaseName + "_" + table.getName());

                    for(Map.Entry<String, String> value: recordsList.entrySet()){
                        String []columns = value.getValue().split(";");
                        if(columns[i-1].equals(recordToBeDeleted.getKey())){
                            recordToBeDeletedDTO.setMessage("FK constraint violated!");
                            return recordToBeDeletedDTO;
                        }
                    }
                }
            }
        }

        repository.deleteRecord(id, databaseTableNames);
        recordToBeDeletedDTO.setRecord(recordToBeDeleted);
        return recordToBeDeletedDTO;
    }
}
