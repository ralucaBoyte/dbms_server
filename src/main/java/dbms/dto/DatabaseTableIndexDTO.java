package dbms.dto;

import dbms.domain.Index;

public class DatabaseTableIndexDTO {
    private String databaseName;
    private String tableName;
    private Index index;

    public DatabaseTableIndexDTO(String databaseName, String tableName, Index index) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.index = index;
    }

    public DatabaseTableIndexDTO() {
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "DatabaseTableIndexDTO{" +
                "databaseName='" + databaseName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", index=" + index +
                '}';
    }
}
