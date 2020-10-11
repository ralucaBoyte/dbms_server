package dbms.dto;

import dbms.domain.Table;

public class DatabaseTableDTO {
    private String databaseName;
    private Table table;

    public DatabaseTableDTO(String databaseName, Table table) {
        this.databaseName = databaseName;
        this.table = table;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    @Override
    public String toString() {
        return "DatabaseTableDTO{" +
                "databaseName='" + databaseName + '\'' +
                ", table=" + table +
                '}';
    }
}
