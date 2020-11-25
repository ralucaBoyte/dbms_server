package dbms.domain;

public class Condition {
    private ConditionType type;
    private String value;

    public Condition(){}

    public Condition(ConditionType type, String value) {
        this.type = type;
        this.value = value;
    }

    public ConditionType getType() {
        return type;
    }

    public void setType(ConditionType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Condition{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
