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
    private String orderBy;

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

    public SelectTableAttributesDTO(String tableName, List<Pair> selectGroupByAttributes, List<Pair> attributeConditions, List<String> groupByAttributes, boolean distinct, String join, Integer limit, String orderBy) {
        this.tableName = tableName;
        this.selectGroupByAttributes = selectGroupByAttributes;
        this.attributeConditions = attributeConditions;
        this.groupByAttributes = groupByAttributes;
        this.distinct = distinct;
        this.join = join;
        this.limit = limit;
        this.orderBy = orderBy;
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

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public String toString() {
        return "SelectTableAttributesDTO{" +
                "tableName='" + tableName + '\'' +
                ", selectGroupByAttributes=" + selectGroupByAttributes +
                ", attributeConditions=" + attributeConditions +
                ", groupByAttributes=" + groupByAttributes +
                ", distinct=" + distinct +
                ", join='" + join + '\'' +
                ", limit=" + limit +
                ", orderBy='" + orderBy + '\'' +
                '}';
    }
}
