package dbms.domain;

import java.util.List;

public class Table {
    private String name, filename;
    private List<Attribute> attributeList;
    private List<Index> indexList;

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", filename='" + filename + '\'' +
                ", attributeList=" + attributeList +
                ", indexList=" + indexList +
                '}';
    }

    public Table(String name, String filename, List<Attribute> attributeList) {
        this.name = name;
        this.filename = filename;
        this.attributeList = attributeList;
    }

    public Table(){}

    public Table(String name, String filename, List<Attribute> attributeList, List<Index> indexList) {
        this.name = name;
        this.filename = filename;
        this.attributeList = attributeList;
        this.indexList = indexList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(List<Attribute> attributeList) {
        this.attributeList = attributeList;
    }
}
