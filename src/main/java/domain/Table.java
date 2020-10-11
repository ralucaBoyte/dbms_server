package domain;

import java.util.List;

public class Table {
    private String name, filename;
    private List<Attribute> primaryKey;
    private List<Attribute> records;

    public Table(String name, String filename,  List<Attribute> primaryKey, List<Attribute> records) {
        this.name = name;
        this.filename = filename;
        this.primaryKey = primaryKey;
        this.records = records;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Attribute> getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(List<Attribute> primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<Attribute> getRecords() {
        return records;
    }

    public void setRecords(List<Attribute> records) {
        this.records = records;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
