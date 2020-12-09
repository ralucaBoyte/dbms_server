package dbms.dto;

import dbms.domain.Pair;

import java.util.List;

public class SelectTableAttributesDTO {
    private String tableName;
    private List<Pair> attributeConditions;
    private boolean distinct;
    private String join;

    public String getJoin() {
        return join;
    }

    public void setJoin(String join) {
        this.join = join;
    }

    public SelectTableAttributesDTO(){}

    public SelectTableAttributesDTO(String tableName, List<Pair> attributeConditions, boolean distinct, String join) {
        this.tableName = tableName;
        this.attributeConditions = attributeConditions;
        this.distinct = distinct;
        this.join = join;
    }


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Pair> getAttributeConditions() {
        return attributeConditions;
    }

    public void setAttributeConditions(List<Pair> attributeConditions) {
        this.attributeConditions = attributeConditions;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public String toString() {
        return "SelectTableAttributesDTO{" +
                "tableName='" + tableName + '\'' +
                ", attributeConditions=" + attributeConditions +
                ", distinct=" + distinct +
                ", join='" + join + '\'' +
                '}';
    }
}
