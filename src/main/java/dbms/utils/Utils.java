package dbms.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import dbms.domain.Database;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static void writeToJSONFile(File file, ObjectMapper objectMapper, List<Database> databaseList){
        try {
            objectMapper.writeValue(file, databaseList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createFolderLocation(String databaseName, String tableName) {

        return "src/main/resources/" + databaseName + "_" + tableName;
    }

    private static String createFileLocation(String folderName, String filename) {

        return folderName + "/" + filename + ".ind";
    }

    public static void createFile(String filename, String databaseName, String tableName) {

        String folderUploadLocation = createFolderLocation(databaseName, tableName);
        String fileUploadLocation = createFileLocation(folderUploadLocation, filename);

        Path newUploadLocation = Paths.get(folderUploadLocation);
        createDirectories(newUploadLocation);

        File f1 = new File(fileUploadLocation);
        try {
            f1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void createDirectories(Path uploadLocation) {

        if (!Files.exists(uploadLocation)) {
            try {
                Files.createDirectories(uploadLocation);
            } catch (IOException e) {
                System.out.println("++++++++create directories failed++++++++");
            }
        }
    }

}
