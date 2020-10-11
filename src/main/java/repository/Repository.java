package repository;


import com.google.gson.JsonParser;
import domain.Database;
import domain.Table;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Repository implements IRepository {
    private List<Database> databases;
    private File file;

    public Repository() {
        this.databases = new ArrayList<>();
        JsonParser parser = new JsonParser();
        try {
            Object obj = parser.parse(new FileReader("Catalog.json"));
            System.out.println(obj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }


    public Database addDatabase(Database database) {
        JSONObject newDatabase = new JSONObject();
        newDatabase.put("databaseName", database.getName());
        newDatabase.put("tables", new ArrayList<Table>());

        try (FileWriter file = new FileWriter("Catalog.json")) {

            file.write(newDatabase.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Table addTable(String databaseName, Table table) {
        return null;
    }

    public Database removeDatabase(String databaseName) {
        return null;
    }

    public Table removeTable(String tableName) {
        return null;
    }
}
