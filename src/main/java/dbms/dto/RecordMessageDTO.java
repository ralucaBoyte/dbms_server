package dbms.dto;

import dbms.domain.Record;

public class RecordMessageDTO {
    private Record record;
    private String message;

    public RecordMessageDTO(){}
    public RecordMessageDTO(Record record, String message) {
        this.record = record;
        this.message = message;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RecordMessageDTO{" +
                "record=" + record +
                ", message='" + message + '\'' +
                '}';
    }
}
