package dbms.domain;

public class Index {
    private String name, filename;
    private Integer isUnique;

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

    @Override
    public String toString() {
        return "Index{" +
                "name='" + name + '\'' +
                ", filename='" + filename + '\'' +
                ", isUnique=" + isUnique +
                '}';
    }
}
