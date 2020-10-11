package domain;

public class Attribute {
    private String name;
    private Type type;
    private Integer notNull, length, unique;


    public Attribute(String name, Type type, Integer notNull, Integer length, Integer unique) {
        this.name = name;
        this.type = type;
        this.notNull = notNull;
        this.length = length;
        this.unique = unique;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getNotNull() {
        return notNull;
    }

    public void setNotNull(Integer notNull) {
        this.notNull = notNull;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getUnique() {
        return unique;
    }

    public void setUnique(Integer unique) {
        this.unique = unique;
    }
}
