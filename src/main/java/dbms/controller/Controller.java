package dbms.controller;

import dbms.domain.*;
import dbms.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import dbms.service.IService;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/dbms")
public class Controller {
    @Autowired
    private IService service;

    private Logger logger = LoggerFactory.getLogger(Controller.class);

    @RequestMapping(value = "/databases", method = RequestMethod.POST)
    public Database addDatabase(@RequestBody Database database) {
        Database newDatabase = service.addDatabase(database);
        logger.info("++++++++++++++++++Added database++++++++++++++++++ ", newDatabase);

        return newDatabase;
    }

    @RequestMapping(value = "/databases/{databaseName}", method = RequestMethod.DELETE)
    public Database removeDatabase(@PathVariable String databaseName) {
        Database newDatabase = service.removeDatabase(databaseName);
        logger.info("++++++++++++++++++Removed database++++++++++++++++++", newDatabase);

        return newDatabase;
    }

    @RequestMapping(value = "/databases", method = RequestMethod.GET)
    public List<Database> getAllDatabases() {
        return service.getAllDatabases();
    }

    @RequestMapping(value = "/tables", method = RequestMethod.POST)
    public DatabaseTableDTO addTable(@RequestBody DatabaseTableDTO databaseTableDTO){
        DatabaseTableDTO newDatabaseTableDTO = service.addTable(databaseTableDTO.getDatabaseName(), databaseTableDTO.getTable());
        logger.info("++++++++++++++++++Added table++++++++++++++++++", newDatabaseTableDTO);
        return newDatabaseTableDTO;
    }

    @RequestMapping(value = "/databases/{databaseName}/tables/{tableName}", method = RequestMethod.DELETE)
    public DatabaseTableDTO removeTable(@PathVariable String databaseName, @PathVariable String tableName) {
        Table table = service.removeTable(databaseName, tableName);
        return new DatabaseTableDTO(databaseName, table);
    }

    @RequestMapping(value = "/index/database/{databaseName}/table/{tableName}", method = RequestMethod.POST)
    public DatabaseTableIndexDTO addIndex(@RequestBody Index index, @PathVariable String databaseName, @PathVariable String tableName){
        Index savedIndex = service.addIndex(index, databaseName, tableName);
        return new DatabaseTableIndexDTO(databaseName, tableName, savedIndex);
    }

    @RequestMapping(value = "/records/{databaseTableNames}", method = RequestMethod.POST)
    public RecordMessageDTO addRecord(@RequestBody Record record, @PathVariable("databaseTableNames") final String databaseTableNames){
        return service.addRecord(record, databaseTableNames);
    }

    @RequestMapping(value = "/records/{databaseTableNames}", method = RequestMethod.GET)
    public List<Record> getAllRecords(@PathVariable("databaseTableNames") final String databaseTableNames){
        return service.findAllRecords(databaseTableNames);
    }

    @RequestMapping(value = "/records/{databaseTableNames}/{id}", method = RequestMethod.DELETE)
    public RecordMessageDTO delete(@PathVariable("databaseTableNames") final String databaseTableNames, @PathVariable("id") final String id) {
        return service.deleteRecord(id, databaseTableNames);
    }

    @RequestMapping(value = "/select/{databaseName}", method = RequestMethod.POST)
    public List<Record> select(@RequestBody SelectTableAttributesDTO selectTableAttributes, @PathVariable("databaseName") final String databaseName){
        List<Record> selectedRecords = service.select(selectTableAttributes, databaseName);
        Integer limit = selectTableAttributes.getLimit();

        if(limit == 0){
            return selectedRecords;
        }
        else{
            return selectedRecords.stream().limit(limit).collect(Collectors.toList());
        }
    }

    @RequestMapping(value = "/select/groupBy/{databaseTableName}", method = RequestMethod.POST)
    public List<Record> groupBy(@RequestBody GroupByDTO groupByDTO, @PathVariable("databaseTableName") final String databaseTableName){
        return service.groupBy(databaseTableName, groupByDTO);
    }

    @RequestMapping(value = "/join/groupBy/{databaseName}", method = RequestMethod.POST)
    public List<Record> groupBy(@RequestBody SelectTableAttributesDTO selectTableAttributesDTO, @PathVariable("databaseName") final String databaseName){
        return service.joinGroupBy(selectTableAttributesDTO, databaseName);
    }
}
