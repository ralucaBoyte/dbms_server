package domain;

public class Attribute {
    private String name;
    private Type type;
    private Integer notNull, length;


    public Attribute(String name, Type type, Integer notNull, Integer length) {
        this.name = name;
        this.type = type;
        this.notNull = notNull;
        this.length = length;
    }

    public Attribute(String name, Type type, Integer notNull) {
        this.name = name;
        this.type = type;
        this.notNull = notNull;
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
}
