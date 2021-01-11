package dbms.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import dbms.domain.*;
import dbms.dto.DatabaseTableDTO;
import dbms.dto.GroupByDTO;
import dbms.dto.RecordMessageDTO;
import dbms.dto.SelectTableAttributesDTO;
import dbms.repository.IRepository;
import dbms.utils.IndexedNestedJoin;
import dbms.utils.Join;
import dbms.utils.MergeSort;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
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
        String indexFilename = databaseName + "_" + tableName + "_";

        String indexName = index.getAttributeList().stream()
                .map(Attribute::getName)
                .collect(Collectors.joining("|"));
        indexName += "Ind";
        indexFilename += indexName;
        index.setFilename(indexFilename);
        index.setName(indexName);
        String databaseTableName = databaseName + "_" + tableName;
        Index indexToBeAdded = repository.addIndex(index, databaseName, tableName);

        List<Attribute> attributeList = repository.findAllAttributesForDB_Table(databaseName, tableName);
        List<Record> recordList = findAllRecords(databaseTableName);
        if(recordList != null){
            addIndexFile(indexFilename, attributeList, indexToBeAdded, recordList);
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


    @Override
    public Record getRecordForGivenIndexFilenameAndField(String indexFilename, String fields){
        String givenValueForKey = repository.getValuesForGivenKey(indexFilename, fields).get(0);
        Record record = new Record(fields, givenValueForKey);
        return record;
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

    public boolean checkJoinedTablesCondition(Condition condition, Record record, Integer tablePosition, Integer attributePosition){
        Double attributeValue;
        switch (condition.getType()) {
            case EQUAL:
                if(condition.getValue().equals(record.getValue().split("\\|")[tablePosition].split("\\;")[attributePosition])) {
                    return true;
                }
                else{
                    return false;
                }
            case LESS_THAN:
                attributeValue = Double.parseDouble(record.getValue().split("\\|")[tablePosition].split("\\;")[attributePosition]);
                if(attributeValue < Double.parseDouble(condition.getValue())){
                    return true;
                }
                else{
                    return false;
                }

            case GREATER_THAN:
                attributeValue = Double.parseDouble(record.getValue().split("\\|")[tablePosition].split("\\;")[attributePosition]);
                if(attributeValue > Double.parseDouble(condition.getValue())){
                    return true;
                }
                else{
                    return false;
                }

            case LESS_EQUAL_THAN:
                attributeValue = Double.parseDouble(record.getValue().split("\\|")[tablePosition].split("\\;")[attributePosition]);
                if(attributeValue <= Double.parseDouble(condition.getValue())){
                    return true;
                }
                else{
                    return false;
                }

            case GREATER_EQUAL_THAN:
                attributeValue = Double.parseDouble(record.getValue().split("\\|")[tablePosition].split("\\;")[attributePosition]);
                if(attributeValue >= Double.parseDouble(condition.getValue())){
                    return true;
                }
                else{
                    return false;
                }
        }
        return false;
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
    public List<Record> select(SelectTableAttributesDTO selectTableAttributes, String databaseName) {
        String tableName = selectTableAttributes.getTableName();
        String joinType = selectTableAttributes.getJoin();
        List<String> tableNames = Arrays.asList(tableName.split("\\|"));

        if(tableNames.size() == 1) {
            List<Record> recordsResults = selectForTable(selectTableAttributes, databaseName);
            return sort(recordsResults, selectTableAttributes.isDistinct(), "values");
        }
        else{
            Table R = repository.getTableByDatabaseName(databaseName, tableNames.get(0));
            List<Table> joinedTables = new ArrayList<>();
            List<Record> joinedRecords = new ArrayList<>();
            joinedTables.add(R);

            for(int i = 1; i < tableNames.size(); i++){
                Table S = repository.getTableByDatabaseName(databaseName, tableNames.get(i));
                switch (joinType){
                    case "INNER JOIN":
                        joinedRecords = Join.join(joinedTables, joinedRecords, databaseName, S, this);
                        //joinedRecords = IndexedNestedJoin.joingUsingIndexes(joinedTables, joinedRecords, S, databaseName, this);
                        break;
                    case "LEFT OUTER JOIN":
                    case "RIGHT OUTER JOIN": {
                        joinedRecords = IndexedNestedJoin.leftOuterJoin(R, S, joinType, joinedTables, databaseName, this);
                        break;
                    }

                }

            }

            List<Record> finalJoinedRecords = joinedRecordsResult(selectTableAttributes.getAttributeConditions(), joinedTables, joinedRecords);
            return finalJoinedRecords;
        }
    }


    public List<Record> joinedRecordsResult(List<Pair> attributeConditions, List<Table> joinedTables, List<Record> joinedRecords){
        if (attributeConditions.size() == 0){
            return joinedRecords;
        }
        else{
            List<Record> finalJoinedRecords = new ArrayList<>();

            ObjectMapper mapper = new ObjectMapper();

            for(Record record: joinedRecords){
                Record recordToBeAdded = new Record();

                recordToBeAdded.setKey("");
                recordToBeAdded.setValue("");

                String recordKey = "";
                String recordValue = "";

                String keyToBeAdded = "";
                String valueToBeAdded = "";

                boolean ok = true;
                for(int i = 0; i < attributeConditions.size() && ok; i++){
                    String tableName = attributeConditions.get(i).getKey().toString().split("\\.")[0];
                    String attributeName = attributeConditions.get(i).getKey().toString().split("\\.")[1];

                    Integer tablePosition = getTablePositionOfGivenTableInJoinedTables(joinedTables, tableName);
                    Integer attributePosition = getPositionOfGivenAttribute(joinedTables.get(tablePosition), attributeName);

                    List<Condition> conditions = new ArrayList<>();
                    List list = mapper.convertValue(attributeConditions.get(i).getValue(), List.class);
                    list.forEach(x->conditions.add(mapper.convertValue(x, Condition.class)));


                    if(conditions.size() == 0){
                        keyToBeAdded = record.getKey().split("\\|")[tablePosition];
                        valueToBeAdded = record.getValue().split("\\|")[tablePosition].split("\\;")[attributePosition];
                        recordKey += keyToBeAdded + "|";
                        recordValue += valueToBeAdded + ";";
                    }
                    else {
                        for (Condition condition : conditions) {
                            if (!checkJoinedTablesCondition(condition, record, tablePosition, attributePosition)) {
                                ok = false;
                                break;
                            } else {
                                keyToBeAdded = record.getKey().split("\\|")[tablePosition];
                                valueToBeAdded = record.getValue().split("\\|")[tablePosition].split("\\;")[attributePosition];
                                recordKey += keyToBeAdded + "|";
                                recordValue += valueToBeAdded + ";";
                            }
                        }
                    }


                    if(ok && i == attributeConditions.size()-1) {
                        recordKey = recordKey.substring(0, recordKey.length() - 1);
                        recordValue = recordValue.substring(0, recordValue.length() - 1);
                        recordToBeAdded.setKey(recordKey);
                        recordToBeAdded.setValue(recordValue);
                        finalJoinedRecords.add(recordToBeAdded);
                    }

                }

            }
            return finalJoinedRecords;
        }

    }


    @Override
    public List<Record> sort(List<Record> records, boolean distinct, String sortingField ) {
        MergeSort mergeSort = new MergeSort();
        Record[] recordsArray = new Record[records.size()];
        records.toArray(recordsArray);
        Record[] result = mergeSort.mergeSort(recordsArray, distinct, sortingField);
        result = Arrays.stream(result).filter(Objects::nonNull).toArray(Record[]::new);
        List<Record> distinctRecords = Arrays.asList(result);
        return distinctRecords;
    }

    public Integer getPositionOfFKAttribute(Table R, Table S){
        List<Attribute> attributeList = R.getAttributeList();

        for(int i = 0; i < attributeList.size(); i++){
            if (attributeList.get(i).getForeignKey() != null && attributeList.get(i).getForeignKey().getKey().equals(S.getName())){
                return i;
            }
        }
        return 0;
    }

    public Integer getTablePositionOfGivenTableInJoinedTables(List<Table> joinedTables, String tableName){

        for(int i = 0; i < joinedTables.size(); i++){
            if (joinedTables.get(i).getName().equals(tableName)){
                return i;
            }
        }
        return -1;
    }

    public Integer getPositionOfGivenAttribute(Table R, String attributeName){
        for(int i = 0; i < R.getAttributeList().size(); i++){
            if (R.getAttributeList().get(i).getName().equals(attributeName)){
                return i;
            }
        }
        return -1;

    }

    @Override
    public List<Record> groupBy(String databaseTableName, GroupByDTO groupByDTO) {
        String databaseName = databaseTableName.split("_")[0];
        String tableName = databaseTableName.split("_")[1];
        List<Pair> selectGroupByAttributes = groupByDTO.getSelectGroupByAttributes();
        List<String> groupByAttributes = groupByDTO.getGroupByAttributes();

        List<Record> records = new ArrayList<>();
        //CHECK IF WE HAVE INDEX ON GROUP BY ATTRIBUTES

        //GROUP BY ON ONE COLUMN
        Map<String, String> groupByResult;
        ObjectMapper mapper = new ObjectMapper();
        //String groupByAttribute = groupByAttributes.get(0);

        String groupByAttribute = String.join("|", groupByAttributes);

        String indexFileName = databaseTableName + "_" + groupByAttribute + "Ind";
        boolean existsIndex = repository.existsIndex(databaseName, tableName, groupByAttribute);

        if(existsIndex) {
            groupByResult = repository.findAllRecords(indexFileName);
            records = aggregateFunctionResult(databaseName, tableName, groupByResult, selectGroupByAttributes);
        }
        else{
            Index indexToBeAdded = new Index();
            String indexName = groupByAttribute + "Ind";
            List<Attribute> attributeList = repository.findAllAttributesForDB_Table(databaseName, tableName);
            List<Attribute> indexAttributeList = attributeList.stream().filter(attribute -> groupByAttributes.contains(attribute.getName())).collect(Collectors.toList());
            indexToBeAdded.setName(indexName);
            indexToBeAdded.setFilename(indexFileName);
            indexToBeAdded.setAttributeList(indexAttributeList);

            addIndex(indexToBeAdded, databaseName, tableName);
            groupByResult = repository.findAllRecords(indexFileName);
            records = aggregateFunctionResult(databaseName, tableName, groupByResult, selectGroupByAttributes);

        }


        return records;
    }

    private List<Record> aggregateFunctionResult(String databaseName, String tableName, Map<String, String> groupByResult, List<Pair> selectAttributes){
        ObjectMapper mapper = new ObjectMapper();
        List<Record> records = new ArrayList<>();

        for (Map.Entry<String,String> entry : groupByResult.entrySet()){

            Record groupByRecord = new Record();

            String groupByAttributeKey = entry.getKey();
            String groupByAttributesValues = entry.getValue();

            groupByRecord.setKey(groupByAttributeKey);
            String recordValue = "";

            for(Pair selectAttribute: selectAttributes){
                String attributeName = (String) selectAttribute.getKey();

                List<String> valuesForGivenAttribute = valuesForGivenAttributeName(databaseName, tableName, groupByAttributesValues, attributeName);

                AggregateFunctions aggregateFunction;
                if(selectAttribute.getValue() != ""){
                    aggregateFunction = mapper.convertValue(selectAttribute.getValue(), AggregateFunctions.class);
                    String finalValueAfterComputingAggregateFunction = computeAggregateFunctionOnGivenList(valuesForGivenAttribute, aggregateFunction);
                    recordValue += finalValueAfterComputingAggregateFunction + ";";
                }
                else{
                    recordValue += valuesForGivenAttribute.get(0) + ";";
                }

            }

            recordValue = recordValue.substring(0, recordValue.length() - 1);
            groupByRecord.setValue(recordValue);
            records.add(groupByRecord);
        }
        return records;
    }

    private String computeAggregateFunctionOnGivenList(List<String> valuesForGivenAttribute, AggregateFunctions aggregateFunctions){
        String aggregateFunctionResult = "";
        List<Double>valuesForGivenAttributeAsNumbers;

        switch (aggregateFunctions){
            case AVG:
                valuesForGivenAttributeAsNumbers = valuesForGivenAttribute.stream().map(Double::parseDouble).collect(Collectors.toList());
                Double average = valuesForGivenAttributeAsNumbers.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
                aggregateFunctionResult = String.valueOf(average);
                break;
            case MAX:
                valuesForGivenAttributeAsNumbers = valuesForGivenAttribute.stream().map(Double::parseDouble).collect(Collectors.toList());
                Double max = valuesForGivenAttributeAsNumbers.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
                aggregateFunctionResult = String.valueOf(max);
                break;
            case MIN:
                valuesForGivenAttributeAsNumbers = valuesForGivenAttribute.stream().map(Double::parseDouble).collect(Collectors.toList());
                Double min = valuesForGivenAttributeAsNumbers.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
                aggregateFunctionResult = String.valueOf(min);
                break;
            case SUM:
                valuesForGivenAttributeAsNumbers = valuesForGivenAttribute.stream().map(Double::parseDouble).collect(Collectors.toList());
                Double sum = valuesForGivenAttributeAsNumbers.stream().mapToDouble(Double::doubleValue).sum();
                aggregateFunctionResult = String.valueOf(sum);
                break;
            case COUNT:
                aggregateFunctionResult = String.valueOf(valuesForGivenAttribute.size());
                break;
        }
        return aggregateFunctionResult;
    }

    private List<Record> getRecordsForGivenPK(String pks, String databaseName, String tableName){
        List<String> primaryKeys = Arrays.asList(pks.split("#"));
        List<Record> records = new ArrayList<>();

        for(String pk: primaryKeys){
            Record record = new Record();
            record.setKey(pk);
            List<String> values = repository.getValuesForGivenKey(databaseName + "_" + tableName, pk);
            String concatenatedValues = values.stream().map(Object::toString)
                    .collect(Collectors.joining(";"));
            record.setValue(concatenatedValues);
            records.add(record);
        }

        return records;
    }

    private List<String> valuesForGivenAttributeName(String databaseName, String tableName, String primaryKeys, String attributeName){
        Table table = repository.getTableByDatabaseName(databaseName, tableName);
        Integer positionOfGivenAttributeInATable = getPositionOfGivenAttribute(table, attributeName);

        List<Record> recordsForTable = getRecordsForGivenPK(primaryKeys, databaseName, tableName);
        List<String> valuesForGivenAttribute = new ArrayList<>();

        for (Record record: recordsForTable) {
            if (positionOfGivenAttributeInATable == 0){
                valuesForGivenAttribute.add(record.getKey());
            }
            else{
                valuesForGivenAttribute.add(record.getValue().split(";")[positionOfGivenAttributeInATable-1]);
            }
        }
        return valuesForGivenAttribute;
    }

}
