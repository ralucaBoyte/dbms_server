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
        //We must save the table before saving indexes
        Table newTable = repository.addTable(databaseName, table);

        List<Attribute> primaryKeys = new ArrayList<>();
        String primaryKeyIndexName = "";

        for(Attribute attribute : table.getAttributeList()){
            if(attribute.getIsUnique() == 1){
                List<Attribute> attributeList = new ArrayList<>();
                attributeList.add(attribute);
                Index index = new Index(attribute.getName() + "Ind", 1, attributeList);
                repository.addIndex(index, databaseName, table.getName());
            }
            if(attribute.getForeignKey() != null){
                List<Attribute> attributeList = new ArrayList<>();
                attributeList.add(attribute);
                Index index = new Index(attribute.getName() + "Ind", 0, attributeList);
                repository.addIndex(index, databaseName, table.getName());

            }

            if(attribute.getIsPrimaryKey() == 1){
                primaryKeys.add(attribute);
                primaryKeyIndexName += attribute.getName() + ";";
            }
        }


        Index index = new Index(primaryKeyIndexName.substring(0, primaryKeyIndexName.length()-1) + "Ind", 0, primaryKeys);
        repository.addIndex(index, databaseName, table.getName());
        return new DatabaseTableDTO(databaseName, newTable);
    }

    @Override
    public Table removeTable(String databaseName, String tableName) {
        repository.deleteAllRecordsFromTable(databaseName+"_"+tableName);
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
    public boolean checkFKContraint(List<Attribute> attributeList, Record record, String databaseName) {

        boolean existsForeignKeyAttribute = true;
        String fk = "";

        for(int i = 0; i < attributeList.size(); i++){
            boolean existingFK = false;
            if(attributeList.get(i).getForeignKey() != null){

                String tableNameFK = attributeList.get(i).getForeignKey().getKey().toString();
                List<Record> recordsForeignKey = findAllRecords(databaseName + "_" + tableNameFK);
                fk = record.getValue().split(";")[i-1];

                for (Record value : recordsForeignKey) {
                    if (value.getKey().equals(fk)) {
                        existingFK = true;
                        break;
                    }
                }

                existsForeignKeyAttribute = existsForeignKeyAttribute & existingFK;
            }
        }
        return existsForeignKeyAttribute;
    }


    @Override
    public RecordMessageDTO addRecord(Record record, String databaseTableNames) {
        String[] parts = databaseTableNames.split("_");
        String databaseName = parts[0];
        String tableName = parts[1];

        RecordMessageDTO recordMessageDTO = new RecordMessageDTO();

        String fk = "";
        List<Attribute> attributeList = repository.findAllAttributesForDB_Table(databaseName, tableName);

        //PRIMARY KEY VERIFICATION USING INDEX FILES
        int pk_number = record.getKey().split(";").length;
        String pkAttributes = "";
        for(int i = 0; i < pk_number; i++){
            pkAttributes += attributeList.get(i).getName();
        }

        String primaryKeyIndexFile = databaseTableNames + "_" + pkAttributes + "Ind";
        Map<String, String> allRecordsFromPKIndex = repository.findAllRecords(primaryKeyIndexFile);

        String recordMessage = "";

        //Check if primary key is unique
        if(allRecordsFromPKIndex.containsKey(record.getKey())) {
            recordMessage += "Primary key must be unique!\n";
        }

        //UNIQUE KEY and FOREIGN KEY VERIFICATION USING INDEX FILES
        List<Pair> uniqueKeys = new ArrayList<>();
        List<Pair> foreignKeys = new ArrayList<>();


         for(int i = pk_number; i < attributeList.size(); i++){
             String attributeName = attributeList.get(i).getName();
             String valueForAttribute = record.getValue().split(";")[i-pk_number];
             if(attributeList.get(i).getIsUnique() == 1){
                 String uniqueKeyIndexFile = databaseTableNames + "_" + attributeName + "Ind";
                 Map<String, String> allRecordsFromUKIndex = repository.findAllRecords(uniqueKeyIndexFile);
                 if(allRecordsFromUKIndex.keySet().stream().anyMatch(unique->unique.equals(valueForAttribute))){
                     recordMessage += attributeName + " is unique!\n";
                 }
                 else{
                     uniqueKeys.add(new Pair(attributeName, new Record(valueForAttribute, record.getKey())));
                 }
             }
             if (attributeList.get(i).getForeignKey() != null){
                 String parentPKIndexFile = databaseName + "_" + attributeList.get(i).getForeignKey().getKey().toString() + "_" + attributeList.get(i).getForeignKey().getValue().toString() + "Ind";
                 Map<String, String> allRecordsFromParentTablePKIndex = repository.findAllRecords(parentPKIndexFile);
                 boolean existsValueInParentTable = allRecordsFromParentTablePKIndex.containsKey(valueForAttribute);
                 if(!existsValueInParentTable){
                     recordMessage += attributeName + " Violation of FK Constraint!\n";
                 }
                 else{
                     String FKIndexFile = databaseTableNames + "_" + attributeName + "Ind";
                     Map<String, String> allRecordsFromFKIndex = repository.findAllRecords(FKIndexFile);
                     String valueForCurrentFK = allRecordsFromFKIndex.get(valueForAttribute);
                     if(valueForCurrentFK == null){
                         foreignKeys.add(new Pair(attributeName, new Record(valueForAttribute, record.getKey())));
                     }
                     else{
                         foreignKeys.add(new Pair(attributeName, new Record(valueForAttribute, valueForCurrentFK + "#" + record.getKey())));
                     }
                 }
             }
         }

        //FOREIGN KEY VERIFICATION USING INDEX FILES
        if(recordMessage != "")
        {
            recordMessageDTO.setMessage(recordMessage);
            return recordMessageDTO;
        }
        else {
            repository.addRecord(new Record(record.getKey(), record.getKey()), primaryKeyIndexFile);
            repository.addRecord(record, databaseTableNames);
            uniqueKeys.forEach(pair -> {
                String uniqueKeyIndexFile = databaseTableNames + "_" + pair.getKey() + "Ind";
                repository.addRecord((Record)pair.getValue(), uniqueKeyIndexFile);
            });
            foreignKeys.forEach(pair -> {
                String FKIndexFile = databaseTableNames + "_" + pair.getKey() + "Ind";
                repository.addRecord((Record)pair.getValue(), FKIndexFile);
            });
            recordMessageDTO.setRecord(record);
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
                    Map<String, String> recordsList = repository.findAllRecords(databaseName + "_" + table.getName() + "_" + attributeList.get(i).getName() + "Ind");

                    if(recordsList.containsKey(recordToBeDeleted.getKey())){
                        recordToBeDeletedDTO.setMessage("FK constraint violated!");
                        return recordToBeDeletedDTO;
                    }
                }
            }
        }

        repository.deleteRecord(id, databaseTableNames);
        recordToBeDeletedDTO.setRecord(recordToBeDeleted);
        return recordToBeDeletedDTO;
    }
}
