package dbms.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import dbms.domain.Database;
import dbms.domain.Table;
import dbms.utils.Utils;


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
        try {
            databaseList = objectMapper.readValue(file, List.class);
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
            if(database.getName()==databaseName){
                List<Table> tableList = database.getTables();
                tableList.add(table);
            }
        });
        Utils.writeToJSONFile(file,objectMapper,databaseList);
        return table;
    }

    public Database removeDatabase(String databaseName) {
        return null;
    }

    public Table removeTable(String tableName) {
        return null;
    }
}
