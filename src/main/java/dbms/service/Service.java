package dbms.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import dbms.domain.*;
import dbms.dto.DatabaseTableDTO;
import dbms.dto.RecordMessageDTO;
import dbms.dto.SelectResultDTO;
import dbms.dto.SelectTableAttributesDTO;
import dbms.repository.IRepository;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
        List<Index> indexList = repository.getAllIndexesForDBandTable(databaseName, tableName);
        indexList.forEach(index -> {
            repository.deleteAllRecordsFromTable(index.getFilename());
        });
        return repository.removeTable(databaseName, tableName);
    }

    public Integer getPositionInKeyOrValueOfRecord(List<Attribute> attributeList, Attribute attribute){
        Integer position = 0;
        for(Attribute attr: attributeList) {
            if (attr.equals(attribute)) {
                break;
            }
            else {
                position++;
            }
        }
        return position;
    }


    public void addIndexFile(String databaseTableNames, List<Attribute> attributeList, Index index, List<Record> records){
        List<Attribute> attributeListForIndex = index.getAttributeList();
        List<Integer> attributesIndexes = new ArrayList<>();

        attributeListForIndex.forEach(attribute -> {
            attributesIndexes.add(getPositionInKeyOrValueOfRecord(attributeList, attribute));
        });

        for (Record record: records){
            addRecordOnNon_UniqueIndex(index, record, attributesIndexes);
        }
    }

    public void addRecordOnNon_UniqueIndex(Index index, Record record, List<Integer> attributesIndexes){
        Record recordToBeAdded = new Record();
        String key = record.getKey();
        List<String> keys = Arrays.asList(key.split(";"));
        String value = record.getValue();
        List<String> values = Arrays.asList(value.split(";"));

        List<String> fullRecord = Stream.concat(keys.stream(), values.stream())
                .collect(Collectors.toList());

        List<String> filtered = attributesIndexes.stream()
                .map(fullRecord::get)
                .collect(Collectors.toList());

        String keyForIndex = String.join(";", filtered);
        String valueForIndex = key;
        Map<String, String> allRecordsFromIndex = repository.findAllRecords(index.getFilename());
        String valueForKey = allRecordsFromIndex.get(keyForIndex);
        if(valueForKey == null){
            recordToBeAdded.setKey(keyForIndex);
            recordToBeAdded.setValue(valueForIndex);
        }
        else{
            recordToBeAdded.setKey(keyForIndex);
            recordToBeAdded.setValue(valueForKey + "#" + key );
        }

        repository.addRecord(recordToBeAdded, index.getFilename());
    }

    @Override
    public Index addIndex(Index index, String databaseName, String tableName) {
        String fileName = index.getAttributeList().stream()
                .map(Attribute::getName)
                .collect(Collectors.joining(""));
        fileName += "_" + databaseName + "_" + tableName;
        index.setFilename(fileName);
        String databaseTableName = databaseName + "_" + tableName;
        Index indexToBeAdded = repository.addIndex(index, databaseName, tableName);

        List<Attribute> attributeList = repository.findAllAttributesForDB_Table(databaseName, tableName);
        List<Record> recordList = findAllRecords(databaseTableName);
        if(recordList != null){
            addIndexFile(fileName, attributeList, indexToBeAdded, recordList);
        }
        return indexToBeAdded;
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
        List<Index> indexList = repository.getAllIndexesForDBandTable(databaseName, tableName);

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
                indexList.removeIf(index -> index.getFilename().equals(uniqueKeyIndexFile));
                repository.addRecord((Record)pair.getValue(), uniqueKeyIndexFile);
            });
            foreignKeys.forEach(pair -> {
                String FKIndexFile = databaseTableNames + "_" + pair.getKey() + "Ind";
                indexList.removeIf(index -> index.getFilename().equals(FKIndexFile));
                repository.addRecord((Record)pair.getValue(), FKIndexFile);
            });
            recordMessageDTO.setRecord(record);
            indexList.forEach(index -> {
                if (index.getAttributeList().get(0).getIsPrimaryKey() != 1) {
                    List<Attribute> attributeListForIndex = index.getAttributeList();
                    List<Integer> attributesIndexes = new ArrayList<>();
                    attributeListForIndex.forEach(attribute -> {
                        attributesIndexes.add(getPositionInKeyOrValueOfRecord(attributeList, attribute));
                    });
                    addRecordOnNon_UniqueIndex(index, record, attributesIndexes);
                }
            });
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

        List<Index> indexList = repository.getAllIndexesForDBandTable(databaseName, tableName);
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


    public List<String> keysVeryfingConditionUsingIndexFile(Map<String, String> keysForIndex, Condition condition){
        List<String> keysVeryfingCondition = new ArrayList<>();
        List<String> values = new ArrayList<>();
        Integer attributeValue;
        for (Map.Entry<String,String> entry : keysForIndex.entrySet()){
            if (entry.getValue() != null){
                values = Arrays.asList(entry.getValue().split("#"));
            }
            switch (condition.getType()) {
                case EQUAL:
                    if(condition.getValue().equals(entry.getKey())) {
                        keysVeryfingCondition.addAll(values);
                    }
                    break;
                case LESS_THAN:
                    attributeValue = Integer.parseInt(entry.getKey());
                    if(attributeValue < Integer.parseInt(condition.getValue())){
                        keysVeryfingCondition.addAll(values);
                    }
                    break;
                case GREATER_THAN:
                    attributeValue = Integer.parseInt(entry.getKey());
                    if(attributeValue > Integer.parseInt(condition.getValue())){
                        keysVeryfingCondition.addAll(values);
                    }
                    break;
                case LESS_EQUAL_THAN:
                    attributeValue = Integer.parseInt(entry.getKey());
                    if(attributeValue <= Integer.parseInt(condition.getValue())){
                        keysVeryfingCondition.addAll(values);
                    }
                    break;
                case GREATER_EQUAL_THAN:
                    attributeValue = Integer.parseInt(entry.getKey());
                    if(attributeValue >= Integer.parseInt(condition.getValue())){
                        keysVeryfingCondition.addAll(values);
                    }
                    break;

            }
        }

        return keysVeryfingCondition.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public void checkCondition(Condition condition, List<Record> records, Integer position){
        Integer attributeValue;
        int i = 0;
        List<String> wholeRecord = new ArrayList<>();

        while( i < records.size()){
            List<String> recordKeys = Arrays.asList(records.get(i).getKey().split(";"));
            List<String> recordValues = Arrays.asList(records.get(i).getValue().split(";"));
            wholeRecord = Stream.concat(recordKeys.stream(), recordValues.stream())
                            .collect(Collectors.toList());
            switch (condition.getType()) {
                case EQUAL:
                    if(!condition.getValue().equals(wholeRecord.get(position))) {
                        records.remove(i);
                    }
                    else{
                        i++;
                    }
                    break;
                case LESS_THAN:
                    attributeValue = Integer.parseInt(wholeRecord.get(position));
                    if(attributeValue >= Integer.parseInt(condition.getValue())){
                        records.remove(i);
                    }
                    else{
                        i++;
                    }
                    break;
                case GREATER_THAN:
                    attributeValue = Integer.parseInt(wholeRecord.get(position));
                    if(attributeValue <= Integer.parseInt(condition.getValue())){
                        records.remove(i);
                    }
                    else{
                        i++;
                    }
                    break;
                case LESS_EQUAL_THAN:
                    attributeValue = Integer.parseInt(wholeRecord.get(position));
                    if(attributeValue > Integer.parseInt(condition.getValue())){
                        records.remove(i);
                    }
                    else{
                        i++;
                    }
                    break;
                case GREATER_EQUAL_THAN:
                    attributeValue = Integer.parseInt(wholeRecord.get(position));
                    if(attributeValue < Integer.parseInt(condition.getValue())){
                        records.remove(i);
                    }
                    else{
                        i++;
                    }
                    break;

            }
        }
    }

    //RESULT OF PROJECTION
    @Override
    public List<Record> selectedRecords(List<Record> records, List<Integer> positions){
        List<Record> selectedRecords = new ArrayList<>();
        List<String> selectedValues = new ArrayList<>();

        for (Record record: records){
            selectedValues.clear();
            String pk = record.getKey();

            List<String> recordKeys = Arrays.asList(record.getKey().split(";"));
            List<String> recordValues = Arrays.asList(record.getValue().split(";"));
            List<String> wholeRecord = Stream.concat(recordKeys.stream(), recordValues.stream())
                    .collect(Collectors.toList());
            List<String> values = Arrays.asList(record.getValue().split(";"));
            for(Integer position : positions){
                selectedValues.add(wholeRecord.get(position));
            }
            selectedRecords.add(new Record(pk, StringUtils.join(selectedValues, ';')));
        }
        return selectedRecords;
    }


    @Override
    public List<Record> selectForTable(SelectTableAttributesDTO selectTableAttribute, String databaseName) {
        ObjectMapper mapper = new ObjectMapper();
        String tableName = selectTableAttribute.getTableName();
        List<Index> indexList = repository.getAllIndexesForDBandTable(databaseName, tableName);
        List<Pair> attributeConditions = selectTableAttribute.getAttributeConditions();
        List<Record> okRecords = findAllRecords(databaseName + "_" + tableName);
        List<Integer> columnsIndexes = new ArrayList<>();
        List<Attribute> attributeList = repository.findAllAttributesForDB_Table(databaseName, tableName);
        //List<Attribute> selectedAttributes = new ArrayList<>();

        for (Pair attributeCondition : attributeConditions) {
            String attributeName = (String) attributeCondition.getKey();

            List<Condition> conditions = new ArrayList<>();
            List list = mapper.convertValue(attributeCondition.getValue(), List.class);
            list.forEach(x->conditions.add(mapper.convertValue(x, Condition.class)));

            String indexName = attributeName + "Ind";
            Optional<Index> existsIndexForAttribute = indexList.stream().filter(index1 -> index1.getName().equals(indexName)).findFirst();


            // ############################################ SAVE POSITIONS OF SELECTED ATTRIBUTES IN A RECORD ##########################################
            Integer position = 0;
            for (int i = 0; i < attributeList.size(); i++) {
                if (attributeList.get(i).getName().equals(attributeName)) {
                    columnsIndexes.add(i);
                    position = i;
                    break;
                }
            }
            // ###############################################################################################################################################


            for (Condition condition : conditions) {
                if (condition.getType() != null) {
                    if (existsIndexForAttribute.isPresent()) {
                        String searchKey = databaseName + "_" + tableName + "_" + indexName;
                        Map<String, String> keysForIndex = repository.findAllRecords(searchKey);
                        List<String> keysVeryfingCondition = keysVeryfingConditionUsingIndexFile(keysForIndex, condition);
                        okRecords.removeIf(record -> !keysVeryfingCondition.contains(record.getKey()));
                    } else {
                        checkCondition(condition, okRecords, position);
                    }
                }
            }
        }
            List<Integer> columnsIndexesWithoutDuplicates = columnsIndexes.stream()
                    .distinct()
                    .collect(Collectors.toList());
            return selectedRecords(okRecords, columnsIndexesWithoutDuplicates);
    }

    @Override
    public List<Record> select(List<SelectTableAttributesDTO> selectTableAttributes, String databaseName) {
        List<Record> recordsResults = selectForTable(selectTableAttributes.get(0), databaseName);
        return recordsResults;
    }
}
