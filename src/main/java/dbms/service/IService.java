package dbms.service;

import dbms.domain.*;
import dbms.dto.*;

import java.util.List;

public interface IService {
    Database addDatabase(Database database);
    Database removeDatabase(String databaseName);
    List<Database> getAllDatabases();
    DatabaseTableDTO addTable(String databaseName, Table table);
    Table removeTable(String databaseName, String tableName);
    void addIndexFile(String databaseTableNames, List<Attribute> attributeList, Index index, List<Record> records);
    Index addIndex(Index index, String databaseName, String tableName);

    boolean checkFKContraint(List<Attribute> attributeList, Record record, String databaseName);
    RecordMessageDTO addRecord(Record record, String databaseTableNames);
    List<Record> findAllRecords(String databaseTableNames);
    Record findRecordById(String id, String databaseTableNames);
    RecordMessageDTO deleteRecord(String id, String databaseTableNames);
   
    List<Record> selectedRecords(List<Record> records, List<Integer> positions);
    List<Record> selectForTable(SelectTableAttributesDTO selectTableAttribute, String databaseName);
    List<Record> select(SelectTableAttributesDTO selectTableAttributes, String databaseName);
    List<Record> sort(List<Record> records, boolean distinct, String sortingField);
    Record getRecordForGivenIndexFilenameAndField(String indexFilename, String fields);
    Integer getPositionOfFKAttribute(Table R, Table S);
    
    List<Record> groupBy(String databaseTableName, GroupByDTO groupByDTO);
    List<Record> joinGroupBy(SelectTableAttributesDTO selectTableAttributesDTO, String databaseName);

}
