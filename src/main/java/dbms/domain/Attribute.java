package dbms.domain;

public class Attribute {
    private String name;
    private Type type;
    private Integer notNull, length, isUnique, isPrimaryKey;
    private Pair foreignKey;


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

    public Integer getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(Integer isUnique) {
        this.isUnique = isUnique;
    }

    public Integer getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(Integer isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public Pair getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(Pair foreignKey) {
        this.foreignKey = foreignKey;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", notNull=" + notNull +
                ", length=" + length +
                ", isUnique=" + isUnique +
                ", isPrimaryKey=" + isPrimaryKey +
                ", foreignKey=" + foreignKey +
                '}';
    }
}
