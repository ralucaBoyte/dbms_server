package dbms.utils;

import dbms.domain.*;
import dbms.service.Service;
import java.util.*;
import java.util.stream.Collectors;

public class Join {

    public Join(){}

    public static List<Record> mergeJoin(List<Table> joinedTables, List<Record> joinedRecords, String databaseName, Table tableS, Service service){
        return join(joinedTables, joinedRecords, databaseName, tableS, service);
    }

    public static Integer getTablePositionLinkedToGivenTable(List<Table> joinedTables, Table S){
        //1 --- IF S IS PARENT TABLE, WE HAVE TO FIND THE TABLE FROM joinedTables WHICH HAS A FK POINTING TO S

        //2 --- IF S IS A CHILD TABLE, WE HAVE TO SEARCH THROUGH THE S's ATTRIBUTE LIST THE FK POINTIN TO ONE OF THE TABLES TABLE FROM joinedTables

        Integer position = 0;

        for(int i = 0; i < joinedTables.size(); ++i){

            if(checkExistingFKToGivenTable(joinedTables.get(i).getAttributeList(), S) || checkExistingFKToGivenTable(S.getAttributeList(), joinedTables.get(i))){
                position = i;
                break;
            }
        }
        return position;
    }

    public static List<Record> join(List<Table> joinedTables, List<Record> joinedRecords, String databaseName, Table S, Service service){
        Integer tableLinkedToSPosition = getTablePositionLinkedToGivenTable(joinedTables, S);

        Table R = joinedTables.get(tableLinkedToSPosition);
        List<Attribute> tableRAttributes = R.getAttributeList();
        List<Attribute> tableSAttributes = S.getAttributeList();

        List<Record> recordsR, recordsS;
        List<Record> orderedRRecords, orderedSRecords;

        Optional<Index> indexFK;

        //table S is parent table
        if(checkExistingFKToGivenTable(tableRAttributes, S)){
            Optional<Attribute> pkAttribute = S.getAttributeList().stream().filter(attribute -> attribute.getIsPrimaryKey() == 1).findFirst();
            recordsS = service.findAllRecords(databaseName + "_" + S.getName());
            indexFK = R.getIndexList().stream().filter(index -> index.getAttributeList().stream().anyMatch(attribute -> attribute.getForeignKey().getKey().equals(S.getName()))).findFirst();
            recordsR = service.findAllRecords(indexFK.get().getFilename());

            if(joinedRecords.size() == 0) {

                //CAZUL IN CARE NU AVEM NIMIC IN JOINED RECORDS - PRIMUL JOIN INTRE 2 TABELE

                orderedRRecords = orderRecordsByKey(recordsR, indexFK.get().getAttributeList().stream().findFirst().get().getType());
                orderedSRecords = orderRecordsByKey(recordsS, S.getAttributeList().stream().filter(attribute -> attribute.getIsPrimaryKey() == 1).findFirst().get().getType());
                joinedRecords = MergeSortJoin.mergeSortJoin(tableLinkedToSPosition,  joinedTables, false, databaseName + "_" + R.getName(), orderedRRecords, orderedSRecords, S, pkAttribute.get().getType(), service);

            }
            else{

                //R - CONTINE joinedRecords
                //S - TABELUL PARINTE

                //fkPosition - pozitia in joinedRecords a atributului care e FK catre S

                Integer fkPosition = service.getPositionOfFKAttribute(joinedTables.get(tableLinkedToSPosition), S);
                String fkType = joinedTables.get(tableLinkedToSPosition).getAttributeList().get(fkPosition).getType();
                joinedRecords = orderJoinedRecordsByForeignKey(joinedRecords, fkType, "V",tableLinkedToSPosition, fkPosition);
                orderedSRecords = orderRecordsByKey(recordsS, S.getAttributeList().stream().filter(attribute -> attribute.getIsPrimaryKey() == 1).findFirst().get().getType());
                joinedRecords = MergeSortJoin.mergeSortJoinForAlreadyJoinedRecords(tableLinkedToSPosition, joinedTables, false, databaseName + "_" + R.getName(), joinedRecords, orderedSRecords, S,  pkAttribute.get().getType(), service);

            }
        }
        else{
            //table R is parent table
            Optional<Attribute> pkAttribute = R.getAttributeList().stream().filter(attribute -> attribute.getIsPrimaryKey() == 1).findFirst();
            recordsR = service.findAllRecords(databaseName + "_" + R.getName());
            indexFK = S.getIndexList().stream().filter(index -> index.getAttributeList().stream().anyMatch(attribute -> attribute.getForeignKey().getKey().equals(R.getName()))).findFirst();
            recordsS = service.findAllRecords(indexFK.get().getFilename());


            if (joinedRecords.size() == 0) {

                //CAZUL IN CARE NU AVEM NIMIC IN JOINED RECORDS - PRIMUL JOIN INTRE 2 TABELE

                orderedRRecords = orderRecordsByKey(recordsR, R.getAttributeList().stream().filter(attribute -> attribute.getIsPrimaryKey() == 1).findFirst().get().getType());
                orderedSRecords = orderRecordsByKey(recordsS, indexFK.get().getAttributeList().stream().findFirst().get().getType());
                joinedRecords = MergeSortJoin.mergeSortJoin(tableLinkedToSPosition, joinedTables,  true, databaseName + "_" + S.getName(), orderedRRecords, orderedSRecords, S, pkAttribute.get().getType(), service);
            }

            else{

                //R - CONTINE joinedRecords
                //S - TABELUL COPIL

                //IN R AVEM TABELUL PARINTE

                Integer fkPosition = service.getPositionOfFKAttribute(S, joinedTables.get(tableLinkedToSPosition));
                String fkType = joinedTables.get(tableLinkedToSPosition).getAttributeList().get(fkPosition).getType();

                recordsS = service.findAllRecords(databaseName + "_" + S.getName());
                joinedRecords = orderJoinedRecordsByForeignKey(joinedRecords, fkType, "K",  tableLinkedToSPosition, fkPosition);

                orderedSRecords = orderRecordsByKey(recordsS, fkType);
                joinedRecords = MergeSortJoin.mergeSortJoinForAlreadyJoinedRecords(tableLinkedToSPosition, joinedTables,  true, databaseName + "_" + S.getName(), joinedRecords, orderedSRecords, S, pkAttribute.get().getType(), service);
            }
        }


        return joinedRecords;
    }


    public static List<Record> orderJoinedRecordsByForeignKey(List<Record> records, String type, String keyOrValue, Integer tablePosition, Integer fkPosition){
        if(fkPosition == 0){
            System.out.println("da");
        }
        List<Record> orderedRecords = new ArrayList<>();
        switch (Type.valueOf(type)){
            case CHAR:
            case VARCHAR:
                if(keyOrValue.equals("V")) {
                    orderedRecords = records
                            .stream()
                            .sorted(Comparator.comparing(x -> x.getValue().split("\\|")[tablePosition].split("\\;")[fkPosition])
                            )
                            .collect(Collectors.toList());
                }
                else{
                    orderedRecords = records
                            .stream()
                            .sorted(Comparator.comparing(x -> x.getKey().split("\\|")[tablePosition])
                            )
                            .collect(Collectors.toList());
                }
                return orderedRecords;
            case INTEGER:
                if(keyOrValue.equals("V")) {
                    orderedRecords = records
                            .stream()
                            .sorted(Comparator.comparingInt(x -> Integer.parseInt(x.getValue().split("\\|")[tablePosition].split("\\;")[fkPosition]))
                            )
                            .collect(Collectors.toList());
                }
                else{
                    orderedRecords = records
                            .stream()
                            .sorted(Comparator.comparingInt(x -> Integer.parseInt(x.getKey().split("\\|")[tablePosition]))
                            )
                            .collect(Collectors.toList());
                }
                return orderedRecords;
            case DOUBLE:
                if(keyOrValue.equals("V")) {
                    orderedRecords = records
                            .stream()
                            .sorted(Comparator.comparingDouble(x -> Double.parseDouble(x.getValue().split("\\|")[tablePosition].split("\\;")[fkPosition]))
                            )
                            .collect(Collectors.toList());
                }
                else{
                    orderedRecords = records
                            .stream()
                            .sorted(Comparator.comparingDouble(x -> Double.parseDouble(x.getKey().split("\\|")[tablePosition]))
                            )
                            .collect(Collectors.toList());
                }
                return orderedRecords;
        }
        return orderedRecords;
    }




    public static List<Record> orderRecordsByKey(List<Record> records, String type){

        List<Record> orderedRecords = new ArrayList<>();
        switch (Type.valueOf(type)){
            case CHAR:
            case VARCHAR:
                orderedRecords = records
                        .stream()
                        .sorted(Comparator.comparing(Record::getKey))
                        .collect(Collectors.toList());
                return orderedRecords;
            case INTEGER:
                orderedRecords = records
                        .stream()
                        .sorted(Comparator.comparingInt(t -> Integer.parseInt(t.getKey())))
                        .collect(Collectors.toList());
                return orderedRecords;
            case DOUBLE:
                orderedRecords = records
                        .stream()
                        .sorted(Comparator.comparingDouble(t -> Double.parseDouble(t.getKey())))
                        .collect(Collectors.toList());
                return orderedRecords;
        }
        return orderedRecords;
    }



    public static boolean checkExistingFKToGivenTable(List<Attribute> tableRAttributes, Table tableS){
        boolean exists = tableRAttributes.stream().anyMatch(attribute -> attribute.getForeignKey() != null && attribute.getForeignKey().getKey().equals(tableS.getName()));
        return exists;
    }


}
