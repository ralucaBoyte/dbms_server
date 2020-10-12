package dbms.domain;

import java.util.List;

public class Table {
    private String name, filename;
    private List<Attribute> records;
    private List<Index> indexList;

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", filename='" + filename + '\'' +
                ", records=" + records +
                ", indexList=" + indexList +
                '}';
    }

    public Table(String name, String filename, List<Attribute> records) {
        this.name = name;
        this.filename = filename;
        this.records = records;
    }

    public Table(){}

    public Table(String name, String filename, List<Attribute> records, List<Index> indexList) {
        this.name = name;
        this.filename = filename;
        this.records = records;
        this.indexList = indexList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<Index> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<Index> indexList) {
        this.indexList = indexList;
    }
}
