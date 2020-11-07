package dbms.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dbms.domain.*;
import dbms.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Repository implements IRepository {
    private List<Database> databaseList;
    private File file;
    private HashOperations hashOperations;
    ObjectMapper objectMapper;
    private RedisTemplate<String, Object> redisTemplate;

    public Repository(RedisTemplate<String, Object> redisTemplate) {
        this.databaseList = new ArrayList<>();
        this.redisTemplate = redisTemplate;
        objectMapper = new ObjectMapper();
        file = new File("Catalog.json");
        hashOperations = this.redisTemplate.opsForHash();
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get("Catalog.json"));
            databaseList = gson.fromJson(reader, new TypeToken<List<Database>>() {}.getType());
            if(databaseList == null){
                this.databaseList = new ArrayList<>();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Database addDatabase(Database database) {
        databaseList.add(database);
        Utils.writeToJSONFile(file,objectMapper,databaseList);
        return database;
    }

    public Table addTable(String databaseName, Table table) {
        databaseList.forEach(database -> {
            if(database.getName().equals(databaseName)){
                List<Table> tableList = database.getTables();
                tableList.add(table);
            }
        });

        Utils.createTablesPath(databaseName, table.getName());
        Utils.writeToJSONFile(file,objectMapper,databaseList);
        return table;
    }

    public Database removeDatabase(String databaseName) {
        Database databaseToBeRomoved = databaseList.stream().filter(d->d.getName().equals(databaseName)).findAny().orElse(null);
        databaseList.removeIf(d -> d.getName().equals(databaseName));
        Utils.writeToJSONFile(file,objectMapper,databaseList);
        return databaseToBeRomoved;
    }

    @Override
    public List<Database> getAllDatabases() {
        return this.databaseList;
    }

    public Table removeTable(String databaseName, String tableName) {

        Table tableToBeRemoved = getTableByDatabaseName(databaseName, tableName);
        databaseList.forEach(database -> {
            if (database.getName().equals(databaseName)){
                List<Table> newTables = database.getTables();
                newTables.removeIf(t->t.getName().equals(tableName));
                database.setTables(newTables);
            }

        });
        Utils.writeToJSONFile(file,objectMapper,databaseList);
        return tableToBeRemoved;
    }

    @Override
    public Table getTableByDatabaseName(String databaseName, String tableName) {
        Database database = databaseList.stream().
                filter(d -> d.getName().equals(databaseName)).
                findAny().orElse(null);
        Table table = database.getTables().stream().
                filter(t->t.getName().equals(tableName)).
                findAny().orElse(null);
        return table;
    }

    @Override
    public Index addIndex(Index index, String databaseName, String tableName) {
        index.setFilename(databaseName + "_" + tableName + "_" + index.getName());

        for(Database database: databaseList){
            if(database.getName().equals(databaseName)){
                List<Table> tableList = database.getTables();
                for(Table table: tableList){
                    if (table.getName().equals(tableName)){
                        List<Index> indexList = table.getIndexList();
                        indexList.add(index);
                        table.setIndexList(indexList);
                    }
                }
            }
        }


//        Utils.createIndexPath(index.getName(), databaseName, tableName);
        Utils.writeToJSONFile(file,objectMapper,databaseList);
        return index;
    }

    @Override
    public List<Attribute> findAllAttributesForDB_Table(String databaseName, String tableName) {


        List<Attribute> attributeList = new ArrayList<>();
        for(int i = 0; i < databaseList.size(); i++){
            if(databaseList.get(i).getName().equals(databaseName)){
                List<Table> tableList = databaseList.get(i).getTables();
                for(int j = 0; j < tableList.size(); j++){
                    if(tableList.get(j).getName().equals(tableName)){
                        attributeList = tableList.get(j).getAttributeList();
                    }
                }
            }
        }
        return attributeList;

    }

    @Override
    public List<Table> getAllTables(String database) {

        for (Database db: databaseList){
            if(db.getName().equals(database)){
                return db.getTables();
            }
        }
        return null;
    }

    @Override
    public List<Index> getAllIndexesForDBandTable(String databaseName, String tableName) {
        List<Index> indexList = new ArrayList<>();
        for(int i = 0; i < databaseList.size(); i++){
            if(databaseList.get(i).getName().equals(databaseName)){
                List<Table> tableList = databaseList.get(i).getTables();
                for(int j = 0; j < tableList.size(); j++){
                    if(tableList.get(j).getName().equals(tableName)){
                        indexList = tableList.get(j).getIndexList();
                    }
                }
            }
        }
        return indexList;
    }

    @Override
    public void addRecord(Record record, String databaseTableNames) {
        hashOperations.put(databaseTableNames, record.getKey(), record.getValue());
    }

    @Override
    public Map<String, String> findAllRecords(String databaseTableNames) {
        return hashOperations.entries(databaseTableNames);
    }

    @Override
    public String findRecordById(String id, String databaseTableNames) {
        return (String)hashOperations.get(databaseTableNames, id);
    }

    @Override
    public void deleteAllRecordsFromTable(String databaseTableName){
        Map<String, String> records = findAllRecords(databaseTableName);
        records.forEach((k,v)->{
            deleteRecord(k,databaseTableName);
        });
    }

    @Override
    public void deleteRecord(String id, String databaseTableName) {
        hashOperations.delete(databaseTableName, id);
    }

    @Override
    public List<Pair> hasForeignKey(Table table) {
        List<Pair> foreign_keys = new ArrayList<>();
        for (Attribute attribute: table.getAttributeList()){
            if (attribute.getForeignKey()!= null){
                foreign_keys.add(attribute.getForeignKey());
            }
        }
        return foreign_keys;
    }

}
