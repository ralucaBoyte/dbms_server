package dbms.utils;

import dbms.domain.Attribute;
import dbms.domain.Index;
import dbms.domain.Record;
import dbms.domain.Table;
import dbms.service.IService;
import dbms.service.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class IndexedNestedJoin {


    public IndexedNestedJoin(){}


    public static List<Record> joingUsingIndexes(List<Table> joinedTables, List<Record> joinedRecords, Table S, String databaseName, Service service){

        if(joinedRecords.size() == 0){
            Table R = joinedTables.get(0);
            joinedRecords = indexedNestedLoopJoinFirst2Tables(R, S, joinedTables, databaseName, service);
            return joinedRecords;
        }

        else {
            //fkTablePosition is the table position in joined Tables where we have a table linked to S - whether it is Parent or Child
            Integer tableLinkedToSPosition = Join.getTablePositionLinkedToGivenTable(joinedTables, S);

            Table R = joinedTables.get(tableLinkedToSPosition);
            List<Attribute> attributeListR = R.getAttributeList();


            List<Record> recordsR = new ArrayList<>();


            boolean SParent = Join.checkExistingFKToGivenTable(attributeListR, S);
            Optional<Index> indexFK;

            if (SParent) {
                Integer fkAttributePosition = service.getPositionOfFKAttribute(R, S);
                List<Record> parentRecords = service.findAllRecords(databaseName + "_" + S.getName());
                indexFK = R.getIndexList().stream().filter(index -> index.getAttributeList().stream().anyMatch(attribute -> attribute.getForeignKey().getKey().equals(S.getName()))).findFirst();
                List<Record> childIndexRecords = service.findAllRecords(indexFK.get().getFilename());
                joinedRecords = indexedNestedLoopJoinParentTable( joinedRecords, parentRecords, childIndexRecords, tableLinkedToSPosition, fkAttributePosition, joinedTables, S.getName(), databaseName, service);
                joinedTables.add(S);
            } else {

                    List<Record> childIndexRecords = new ArrayList<>();
                    Integer fkAttributePosition = service.getPositionOfFKAttribute(S, R);
                    indexFK = S.getIndexList().stream().filter(index -> index.getAttributeList().stream().anyMatch(attribute -> attribute.getForeignKey().getKey().equals(R.getName()))).findFirst();
                    childIndexRecords = service.findAllRecords(indexFK.get().getFilename());
                    joinedRecords = indexedNestedLoopJoinAdditionalTable( joinedRecords, childIndexRecords, tableLinkedToSPosition, fkAttributePosition, joinedTables, S.getName(), databaseName, service);
                    joinedTables.add(S);
            }

            return joinedRecords;
        }
    }

    private static List<Record> indexedNestedLoopJoinParentTable(List<Record> joinedRecords, List<Record> parentRecords, List<Record> indexChildRecords, Integer tableLinkedToSPosition, Integer fkAttributePosition, List<Table> joinedTables, String childNameTable, String databaseName, Service service) {

        for (Record parentRecord: parentRecords){
            for(Record indexChildRecord: indexChildRecords){
                mergeRecordsWithParent(parentRecord, indexChildRecord, joinedRecords, tableLinkedToSPosition, fkAttributePosition, databaseName, childNameTable, service);
            }
        }
        return joinedRecords;
    }


    private static List<Record> indexedNestedLoopJoinAdditionalTable(List<Record> joinedRecords, List<Record> childRecords, Integer tableLinkedToSPosition, Integer fkAttributePosition, List<Table> joinedTables, String childNameTable, String databaseName, Service service) {

            return indexedNestedLoopJoin(false, joinedRecords, childRecords, tableLinkedToSPosition, fkAttributePosition, databaseName, childNameTable, service);
    }

    private static List<Record> indexedNestedLoopJoinFirst2Tables(Table R, Table S, List<Table> joinedTables, String databaseName, Service service){
        List<Attribute> tableRAttributes = R.getAttributeList();
        Optional<Index> indexFK;

        Table parent, child;
        joinedTables.clear();

        if(Join.checkExistingFKToGivenTable(tableRAttributes, S)){
            parent = S;
            child = R;
            joinedTables.add(S);
            joinedTables.add(R);
        }
        else{
            //R is parent table
            parent = R;
            child = S;
            joinedTables.add(R);
            joinedTables.add(S);
        }

        indexFK = child.getIndexList().stream().filter(index -> index.getAttributeList().stream().anyMatch(attribute -> attribute.getForeignKey().getKey().equals(parent.getName()))).findFirst();
        List<Record> parentsRecords = service.findAllRecords(databaseName + "_" + parent.getName());
        List<Record> childRecords = service.findAllRecords(indexFK.get().getFilename());
        return indexedNestedLoopJoin(true, parentsRecords, childRecords, 0, 0,databaseName, child.getName(), service);

    }

    public static List<Record> leftOuterJoin(Table R, Table S, List<Table> joinedTables, String databaseName, Service service) {
        List<Attribute> tableRAttributes = R.getAttributeList();
        List<Record> joinedRecords = new ArrayList<>();
        List<Record> rRecords, sRecords;

        Optional<Index> indexFK;

        boolean SParent = Join.checkExistingFKToGivenTable(tableRAttributes, S);

        //joinedTables.add(S);
        if(SParent){

            return indexedNestedLoopJoinFirst2Tables(S, R, joinedTables, databaseName, service);
        }
        else{
            rRecords = service.findAllRecords(databaseName + "_" + R.getName());
            indexFK = S.getIndexList().stream().filter(index -> index.getAttributeList().stream().anyMatch(attribute -> attribute.getForeignKey().getKey().equals(R.getName()))).findFirst();
            sRecords = service.findAllRecords(indexFK.get().getFilename());
            joinedRecords = nestedLoopLeftOuterJoin(rRecords, sRecords, databaseName, S.getName(), service);
            return joinedRecords;
        }
    }

    private static List<Record> nestedLoopLeftOuterJoin(List<Record> parentRecords, List<Record> indexedChildRecords, String databaseName, String childTableName, Service service){
        List<Record> joinedRecords = new ArrayList<>();

        Integer attributesForChildRecord = service.findAllRecords(databaseName + "_" + childTableName).get(0).getValue().split(";").length + 1;

        boolean merged = false;
        for (Record parentRecord: parentRecords){

            merged = false;
            for(Record indexChildRecord: indexedChildRecords){
                Record record = new Record();
                if(parentRecord.getKey().equals(indexChildRecord.getKey())) {
                    merged = true;
                    List<Record> childRecords = getRecordsFromIndexValue(indexChildRecord.getValue(), databaseName, childTableName, service);
                    for (Record childRecord: childRecords){
                        record.setKey(parentRecord.getKey() + "|" + childRecord.getKey());
                        record.setValue(parentRecord.getValue() + "|" + childRecord.getKey()+";"+childRecord.getValue());
                        joinedRecords.add(record);
                    }
                }

            }

            if(merged == false){
                Record record = new Record();
                record.setKey(parentRecord.getKey());
                record.setValue(parentRecord.getValue() + "|NULL");
                for(int i = 0; i < attributesForChildRecord-1; i++){
                    record.setValue(record.getValue() + ";NULL");
                }
                joinedRecords.add(record);

            }
        }
        return joinedRecords;
    }


    private static List<Record> indexedNestedLoopJoin(boolean first2Join, List<Record> parentRecords, List<Record> indexedChildRecords, Integer tableLinkedToSPosition, Integer fkAttributePosition, String databaseName, String childTableName, Service service){
        List<Record> joinedRecords = new ArrayList<>();

        for (Record parentRecord: parentRecords){
            for(Record indexChildRecord: indexedChildRecords){
                if (first2Join){
                    mergeRecordsFirst2Tables(parentRecord, indexChildRecord, joinedRecords, databaseName, childTableName, service);
                }
                else{
                    mergeRecordsAdditionalTable(parentRecord, indexChildRecord, joinedRecords, tableLinkedToSPosition, databaseName, childTableName, service);
                }
            }
        }
        return joinedRecords;
    }

    private static List<Record> mergeRecordsWithParent(Record parentRecord, Record indexChildRecord, List<Record> joinedRecords, Integer tableLinkedPosition, Integer fkAttributePosition, String databaseName, String childTableName, Service service){
        if(parentRecord.getKey().equals(indexChildRecord.getKey())){
            getMatchingJoinedRecords(parentRecord, joinedRecords, indexChildRecord.getValue(), tableLinkedPosition, fkAttributePosition);
        }
        return joinedRecords;
    }

    private static void getMatchingJoinedRecords(Record parentRecord, List<Record> joinedRecords, String recordIds, Integer tableLinkedPosition, Integer fkAttributePosition){
        List<String> childIds = Arrays.asList(recordIds.split("#"));
        for(Record record: joinedRecords){
            String value = record.getValue().split("\\|")[tableLinkedPosition].split("\\;")[0];
            if(childIds.contains(value)){
                record.setKey(record.getKey() + "|" + parentRecord.getKey());
                record.setValue(record.getValue() + "|" + parentRecord.getKey() + ";" + parentRecord.getValue());
            }
        }


    }


    private static List<Record> mergeRecordsFirst2Tables(Record parentRecord, Record indexChildRecord, List<Record> joinedRecords, String databaseName, String childTableName, Service service){
        if(parentRecord.getKey().equals(indexChildRecord.getKey())){
            List<Record> childRecords = getRecordsFromIndexValue(indexChildRecord.getValue(), databaseName, childTableName, service);
            joinedRecords.addAll(joinedParentRecordWithChildRecords(parentRecord, childRecords));
        }
        return joinedRecords;
    }

    private static List<Record> mergeRecordsAdditionalTable(Record parentRecord, Record indexChildRecord, List<Record> joinedRecords, Integer tableLinkedPosition, String databaseName, String childTableName, Service service){

        String parentPrimaryKey = parentRecord.getKey().split("|")[tableLinkedPosition];
        if(parentPrimaryKey.equals(indexChildRecord.getKey())){
            List<Record> childRecords = getRecordsFromIndexValue(indexChildRecord.getValue(), databaseName, childTableName, service);
            joinedRecords.addAll(joinAdditionalTableRecords(parentRecord, childRecords));
        }

        return joinedRecords;
    }

    private static List<Record> joinAdditionalTableRecords(Record parentRecord, List<Record> childRecords){
        List<Record> joinedRecords = new ArrayList<>();

        for(Record childRecord: childRecords){
            Record record = new Record();
            record.setKey(parentRecord.getKey() + "|" + childRecord.getKey());
            record.setValue(parentRecord.getValue() + "|" + childRecord.getKey()+";"+childRecord.getValue());
            joinedRecords.add(record);
        }

        return joinedRecords;
    }

    private static List<Record> joinedParentRecordWithChildRecords(Record parentRecord, List<Record> childRecords){
        List<Record> joinedRecords = new ArrayList<>();

        for(Record childRecord: childRecords){
            Record record = new Record();
            record.setKey(parentRecord.getKey() + "|" + childRecord.getKey());
            record.setValue(parentRecord.getKey()+";"+parentRecord.getValue() + "|" + childRecord.getKey()+";"+childRecord.getValue());
            joinedRecords.add(record);
        }

        return joinedRecords;
    }

    private static List<Record> joinChildRecordWithParent(Record parentRecord, List<Record> childRecords){
        List<Record> joinedRecords = new ArrayList<>();

        for(Record childRecord: childRecords){
            Record record = new Record();
            record.setKey(parentRecord.getKey() + "|" + childRecord.getKey());
            record.setValue(parentRecord.getKey()+";"+parentRecord.getValue() + "|" + childRecord.getKey()+";"+childRecord.getValue());
            joinedRecords.add(record);
        }

        return joinedRecords;
    }


    private static List<Record> getRecordsFromIndexValue(String value, String databaseName, String tableName, Service service){
        List<String> childPrimaryKeys = Arrays.asList(value.split("#"));
        List<Record> childRecords = new ArrayList<>();

        for (String childPrimaryKey: childPrimaryKeys){
            Record record = service.findRecordById(childPrimaryKey, databaseName + "_" + tableName);
            childRecords.add(record);
        }
        return childRecords;
    }


}
