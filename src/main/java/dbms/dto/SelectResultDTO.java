package dbms.dto;

import dbms.domain.Attribute;
import dbms.domain.Record;

import java.util.List;

public class SelectResultDTO {
    private String tableName;
    private List<Record> data;

    public SelectResultDTO() {}

    public SelectResultDTO(String tableName, List<Record> data) {
        this.tableName = tableName;
        this.data = data;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Record> getData() {
        return data;
    }

    public void setData(List<Record> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SelectResultDTO{" +
                "tableName='" + tableName + '\'' +
                ", data=" + data +
                '}';
    }
}
