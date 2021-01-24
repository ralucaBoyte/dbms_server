package dbms.dto;

import dbms.domain.Pair;

import java.util.List;

public class SelectTableAttributesDTO {
    private String tableName;
    private List<Pair> selectGroupByAttributes;
    private List<Pair> attributeConditions;
    private List<String> groupByAttributes;
    private boolean distinct;
    private String join;
    private Integer limit;

    public SelectTableAttributesDTO(String tableName, List<Pair> selectGroupByAttributes, List<Pair> attributeConditions, List<String> groupByAttributes, boolean distinct, String join) {
        this.tableName = tableName;
        this.selectGroupByAttributes = selectGroupByAttributes;
        this.attributeConditions = attributeConditions;
        this.groupByAttributes = groupByAttributes;
        this.distinct = distinct;
        this.join = join;
    }

    public SelectTableAttributesDTO(String tableName, List<Pair> selectGroupByAttributes, List<Pair> attributeConditions, List<String> groupByAttributes, boolean distinct, String join, Integer limit) {
        this.tableName = tableName;
        this.selectGroupByAttributes = selectGroupByAttributes;
        this.attributeConditions = attributeConditions;
        this.groupByAttributes = groupByAttributes;
        this.distinct = distinct;
        this.join = join;
        this.limit = limit;
    }

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

    public List<Pair> getSelectGroupByAttributes() {
        return selectGroupByAttributes;
    }

    public void setSelectGroupByAttributes(List<Pair> selectGroupByAttributes) {
        this.selectGroupByAttributes = selectGroupByAttributes;
    }

    public List<String> getGroupByAttributes() {
        return groupByAttributes;
    }

    public void setGroupByAttributes(List<String> groupByAttributes) {
        this.groupByAttributes = groupByAttributes;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
