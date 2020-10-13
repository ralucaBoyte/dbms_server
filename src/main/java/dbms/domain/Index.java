package dbms.domain;

import java.util.ArrayList;
import java.util.List;

public class Index {
    private String name, filename;
    private Integer isUnique;
    private List<Attribute> attributeList;

    public Index(String name, String filename, Integer isUnique, List<Attribute> attributeList) {
        this.name = name;
        this.filename = filename;
        this.isUnique = isUnique;
        this.attributeList = attributeList;
    }

    public Index(String name, List<Attribute> attributeList) {
        this.name = name;
        this.filename = "";
        this.isUnique = 0;
        this.attributeList = attributeList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Index(String name, String filename, Integer isUnique) {
        this.name = name;
        this.filename = filename;
        this.isUnique = isUnique;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(Integer isUnique) {
        this.isUnique = isUnique;
    }

    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(List<Attribute> attributeList) {
        this.attributeList = attributeList;
    }

    @Override
    public String toString() {
        return "Index{" +
                "name='" + name + '\'' +
                ", filename='" + filename + '\'' +
                ", isUnique=" + isUnique +
                ", attributeList=" + attributeList +
                '}';
    }
}
