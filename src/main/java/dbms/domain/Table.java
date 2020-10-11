package dbms.domain;

import java.util.List;

public class Table {
    private String name, filename;
    private List<Attribute> primaryKeys;
    private List<Attribute> records;
    private List<Pair> foreignKeys;
    private List<Index> indexList;

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", filename='" + filename + '\'' +
                ", primaryKeys=" + primaryKeys +
                ", records=" + records +
                ", foreignKeys=" + foreignKeys +
                ", indexList=" + indexList +
                '}';
    }

    public Table(String name, String filename, List<Attribute> primaryKeys, List<Attribute> records) {
        this.name = name;
        this.filename = filename;
        this.primaryKeys = primaryKeys;
        this.records = records;
    }

    public Table(){}

    public Table(String name, String filename, List<Attribute> primaryKeys, List<Attribute> records, List<Pair> foreignKeys, List<Index> indexList) {
        this.name = name;
        this.filename = filename;
        this.primaryKeys = primaryKeys;
        this.records = records;
        this.foreignKeys = foreignKeys;
        this.indexList = indexList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Attribute> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<Attribute> primaryKeys) {
        this.primaryKeys = primaryKeys;
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

    public List<Pair> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(List<Pair> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public List<Index> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<Index> indexList) {
        this.indexList = indexList;
    }
}
