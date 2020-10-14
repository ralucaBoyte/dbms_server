package dbms.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import dbms.domain.Database;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.util.List;

public class Utils {

    public static String DB_PATH = "src/main/resources/";
    public static void writeToJSONFile(File file, ObjectMapper objectMapper, List<Database> databaseList){
        try {
            objectMapper.writeValue(file, databaseList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static String createIndexFilename(String filename) {
        return filename + ".ind";
    }

    public static String createTableFilename(String filename) {
        return filename + ".kv";
    }


    public static void createFile(String fileUploadLocation) {

        File f1 = new File(fileUploadLocation);
        try {
            f1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void createDirectories(Path uploadLocation) {

        if (!Files.exists(uploadLocation)) {
            try {
                Files.createDirectories(uploadLocation);
            } catch (IOException e) {
                System.out.println("++++++++create directories failed++++++++");
            }
        }
    }

    public static void createTablesPath(String databaseName, String tableName){
        String directoryName = DB_PATH + databaseName;
        Path directoryPath = Paths.get(directoryName);
        Utils.createDirectories(directoryPath);


        String tablesPath = directoryName + "/tables";
        Path tablePath = Paths.get(tablesPath);
        Utils.createDirectories(tablePath);


        String tableNamePath = tablesPath + "/" + tableName;
        createFile(createTableFilename(tableNamePath));
    }

    public static void createIndexPath(String indexName, String databaseName, String tableName){
        String directoryName = DB_PATH + databaseName;
        Path directoryPath = Paths.get(directoryName);
        Utils.createDirectories(directoryPath);

        String tableNamePath = directoryName + "/" + tableName;
        Path tablePath = Paths.get(tableNamePath);
        Utils.createDirectories(tablePath);


        String indexNamePath = tableNamePath + "/indexes";
        Path indexPath = Paths.get(indexNamePath);
        Utils.createDirectories(indexPath);


        String index_name = indexNamePath + "/" + indexName + "_" + databaseName + "_" + tableName;
        createFile(createIndexFilename(index_name));
    }

}
