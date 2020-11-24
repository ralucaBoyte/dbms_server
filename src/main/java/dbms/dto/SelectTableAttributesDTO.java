package dbms.dto;

import dbms.domain.Pair;

import java.util.List;

public class SelectTableAttributesDTO {
    private String tableName;
    private List<Pair> attributeConditions;

    public SelectTableAttributesDTO(String tableName, List<Pair> attributeConditions) {
        this.tableName = tableName;
        this.attributeConditions = attributeConditions;
    }

    @Override
    public String toString() {
        return "SelectTableAttributes{" +
                "tableName='" + tableName + '\'' +
                ", attributeConditions=" + attributeConditions +
                '}';
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
}
