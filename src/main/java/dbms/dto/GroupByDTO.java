package dbms.dto;

import dbms.domain.Pair;

import java.util.List;

public class GroupByDTO {
    private List<Pair> selectGroupByAttributes;
    private List<String> groupByAttributes;


    public GroupByDTO(List<Pair> selectGroupByAttributes, List<String> groupByAttributes) {
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
}
