package dbms.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import dbms.domain.Database;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class Utils {

    public static void writeToJSONFile(File file, ObjectMapper objectMapper, List<Database> databaseList){
        try {
            objectMapper.writeValue(file, databaseList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
