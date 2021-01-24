package dbms.dto;

import dbms.domain.Pair;

import java.util.List;

public class JoinGroupByDTO {

    private String tableNames;
    private List<Pair> selectGroupByAttributes;
    private List<String> groupByAttributes;


    public JoinGroupByDTO(String tableNames, List<Pair> selectGroupByAttributes, List<String> groupByAttributes) {
        this.tableNames = tableNames;
        this.selectGroupByAttributes = selectGroupByAttributes;
        this.groupByAttributes = groupByAttributes;
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

    public String getTableNames() {
        return tableNames;
    }

    public void setTableNames(String tableNames) {
        this.tableNames = tableNames;
    }
}
