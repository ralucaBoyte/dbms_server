package dbms.utils;

import dbms.domain.Record;
import dbms.domain.Table;
import dbms.domain.Type;
import dbms.service.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeSortJoin {

    public static List<Record> mergeSortJoinForAlreadyJoinedRecords(Integer linkedTablePosition, List<Table> joinedTables, boolean parentFirst, String indexFilename, List<Record> R, List<Record> S, Table tableS, String type, Service service){
        List<Record> joinedRecords = new ArrayList<>();
        Integer r_index = 0, s_index = 0;
        Integer mark = -1;
        Record currentParentRecord;
        List<Record> recordsFromChildTable;
        Integer fkPositionValue;
        if(parentFirst){
            fkPositionValue = service.getPositionOfFKAttribute(tableS, joinedTables.get(linkedTablePosition));
        }
        else {
            fkPositionValue = service.getPositionOfFKAttribute(joinedTables.get(linkedTablePosition), tableS);
        }

        while (r_index < R.size() && s_index < S.size()) {
            if (mark == -1) {

                while (compareJoinedRecordsKeys(linkedTablePosition, fkPositionValue, parentFirst, R.get(r_index), S.get(s_index), type) < 0) {
                    r_index++;
                }

                while (compareJoinedRecordsKeys(linkedTablePosition, fkPositionValue, parentFirst, R.get(r_index), S.get(s_index), type) > 0) {
                    s_index++;
                }
                mark = s_index;
            }

            if (compareJoinedRecordsKeys(linkedTablePosition, fkPositionValue, parentFirst, R.get(r_index), S.get(s_index), type) == 0) {

                if(parentFirst) {

                    currentParentRecord = R.get(r_index);
                    //fkPositionValue = service.getPositionOfFKAttribute(joinedTables.get(linkedTablePosition), tableS);

                    Record childRecord = S.get(s_index);
                    Record recordToBeAdded = new Record();
                    recordToBeAdded.setKey(currentParentRecord.getKey() + "|" + childRecord.getKey());
                    recordToBeAdded.setValue(currentParentRecord.getValue() + "|" + childRecord.getKey()+";"+childRecord.getValue());

                    joinedRecords.add(recordToBeAdded);
                }
                else {
                    currentParentRecord = S.get(s_index);
                    //fkPositionValue = service.getPositionOfFKAttribute(joinedTables.get(linkedTablePosition), tableS);

                    Record childRecord = R.get(r_index);
                    Record recordToBeAdded = new Record();
                    recordToBeAdded.setKey(childRecord.getKey() + "|" + currentParentRecord.getKey());
                    recordToBeAdded.setValue(childRecord.getValue() + "|" + currentParentRecord.getKey() + ";" +currentParentRecord.getValue());
                    joinedRecords.add(recordToBeAdded);

                }

                s_index++;
            } else {
                s_index = mark;
                r_index++;
                mark = -1;
            }
        }
        return joinedRecords;

    }

    public static List<Record> mergeSortJoin(Integer linkedTablePosition, List<Table> joinedTables, boolean parentFirst, String indexFilename, List<Record> R, List<Record> S, Table tableS, String type, Service service) {
        List<Record> joinedRecords = new ArrayList<>();
        Integer r_index = 0, s_index = 0;
        Integer mark = -1;
        Record currentParentRecord;
        List<Record> recordsFromChildTable;
        Integer fkPositionValue = service.getPositionOfFKAttribute(joinedTables.get(linkedTablePosition), tableS);

        while (r_index < R.size() && s_index < S.size()) {
            if (mark == -1) {

                while (compareRecordsKeys(linkedTablePosition, R.get(r_index), S.get(s_index), type) < 0) {
                    r_index++;
                }

                while (compareRecordsKeys(linkedTablePosition, R.get(r_index), S.get(s_index), type) > 0) {
                    s_index++;
                }
                mark = s_index;
            }

            if (compareRecordsKeys(linkedTablePosition,  R.get(r_index), S.get(s_index), type) == 0) {

                if(parentFirst) {

                    currentParentRecord = R.get(r_index);
                    fkPositionValue = service.getPositionOfFKAttribute(joinedTables.get(linkedTablePosition), tableS);

                    recordsFromChildTable = getRecordsForFKIndex(indexFilename, S.get(s_index), service);


                    for (Record record : recordsFromChildTable) {
                        Record recordToBeAdded = new Record();
                        recordToBeAdded.setKey(currentParentRecord.getKey() + "|" + record.getKey());
                        recordToBeAdded.setValue(currentParentRecord.getKey()+";"+currentParentRecord.getValue() + "|" + record.getKey()+";"+record.getValue());

                        joinedRecords.add(recordToBeAdded);
                    }

                }
                else {
                    currentParentRecord = S.get(s_index);
                    fkPositionValue = service.getPositionOfFKAttribute(joinedTables.get(linkedTablePosition), tableS);

                    recordsFromChildTable = getRecordsForFKIndex(indexFilename, R.get(r_index), service);


                    for (Record record : recordsFromChildTable) {
                        Record recordToBeAdded = new Record();
                        recordToBeAdded.setKey(record.getKey() + "|" + currentParentRecord.getKey());
                        recordToBeAdded.setValue(record.getKey() + ";"+record.getValue() + "|" + currentParentRecord.getKey() + ";" + currentParentRecord.getValue());
                        joinedRecords.add(recordToBeAdded);
                    }
                }

                s_index++;
            } else {
                s_index = mark;
                r_index++;
                mark = -1;
            }
        }
        return joinedRecords;
    }

    public static Integer compareRecordsKeys(Integer tablePosition, Record r1, Record r2, String type){

        switch (Type.valueOf(type)){
            case CHAR:
            case VARCHAR:
                return r1.getKey().split("\\|")[tablePosition].compareTo(r2.getKey());
            case INTEGER:
                return Integer.compare(Integer.parseInt(r1.getKey().split("\\|")[tablePosition]), Integer.parseInt(r2.getKey()));
            case DOUBLE:
                return Double.compare(Double.parseDouble(r1.getKey().split("\\|")[tablePosition]), Double.parseDouble(r2.getKey()));
        }
        return r1.getKey().split("\\|")[tablePosition].compareTo(r2.getKey());
    }

    public static Integer compareJoinedRecordsKeys(Integer tablePosition, Integer fkPosition, Boolean keyOrValue, Record r1, Record r2, String type) {
        switch (Type.valueOf(type)) {
            case CHAR:
            case VARCHAR:
                if (keyOrValue.equals(false)) {
                    return r1.getValue().split("\\|")[tablePosition].split("\\;")[fkPosition].compareTo(r2.getKey());
                } else {
                    return r1.getKey().split("\\|")[tablePosition].compareTo(r2.getValue().split("\\;")[fkPosition-1]);
                }

            case INTEGER:
                if (keyOrValue.equals(false)) {
                    return Integer.compare(Integer.parseInt(r1.getValue().split("\\|")[tablePosition].split("\\;")[fkPosition]), Integer.parseInt(r2.getKey()));
                } else {
                    return Integer.compare(Integer.parseInt(r1.getKey().split("\\|")[tablePosition]), Integer.parseInt(r2.getValue().split("\\;")[fkPosition-1]));
                }
            case DOUBLE:
                if (keyOrValue.equals(false)) {
                    return Double.compare(Double.parseDouble(r1.getValue().split("\\|")[tablePosition].split("\\;")[fkPosition]), Double.parseDouble(r2.getKey()));
                } else {
                    return Double.compare(Double.parseDouble(r1.getKey().split("\\|")[tablePosition]), Double.parseDouble(r2.getValue().split("\\;")[fkPosition-1]));
                }
        }
        return r1.getValue().split("\\|")[tablePosition].split("\\;")[fkPosition].compareTo(r2.getKey());
    }

    public static List<Record> getRecordsForFKIndex(String indexFilename, Record record, Service service){
        List<Record> recordListForFKIndex = new ArrayList<>();
        List<String> values = Arrays.asList(record.getValue().split("#"));

        for(String pkValue: values){
            Record recordToBeAdded = service.getRecordForGivenIndexFilenameAndField(indexFilename, pkValue);
            recordListForFKIndex.add(recordToBeAdded);
        }

        return recordListForFKIndex;
    }

    public static List<Record> getRecordsFromJoinedRecordsLinkedToParentRecord(Integer linkedTablePosition, Integer fkPositionValue, List<Record> records, Record parentRecord){
        List<Record> newRecords = new ArrayList<>();
        for (Record record: records){
            String recordValue = record.getValue().split("\\|")[linkedTablePosition];
            String fk = recordValue.split(";")[fkPositionValue-1];
            if(fk.equals(parentRecord.getKey())){
                newRecords.add(record);
            }
        }
        return newRecords;
    }



}
