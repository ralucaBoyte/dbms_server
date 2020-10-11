package dbms.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dbms.domain.Database;
import dbms.domain.Table;
import dbms.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Type;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Repository implements IRepository {
    private List<Database> databaseList;
    private File file;
    ObjectMapper objectMapper;

    public Repository() {
        this.databaseList = new ArrayList<>();
        objectMapper = new ObjectMapper();
        file = new File("Catalog.json");
        JSONParser parser = new JSONParser();
        try {
            JSONArray databases = (JSONArray) parser.parse(new FileReader("Catalog.json"));
            Gson gson = new Gson();
            Type type = new TypeToken<List<Database>>(){}.getType();
            this.databaseList = gson.fromJson(databases.toJSONString(), type);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        try {
//            databaseList = objectMapper.readValue(file, List.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
        Utils.writeToJSONFile(file,objectMapper,databaseList);
        return table;
    }

    public Database removeDatabase(String databaseName) {
        databaseList.removeIf(d -> d.getName().equals(databaseName));
        Utils.writeToJSONFile(file,objectMapper,databaseList);
        return null;
    }

    public Table removeTable(String databaseName, String tableName) {
        databaseList.forEach(database -> {
            if (database.getName().equals(databaseName)){
                List<Table> newTables = database.getTables();
                newTables.removeIf(t->t.getName().equals(tableName));
                database.setTables(newTables);
            }
            System.out.println(database);
        });
        return null;
    }
}
