package dbms.controller;

import dbms.domain.*;
import dbms.dto.DatabaseTableDTO;
import dbms.dto.DatabaseTableIndexDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import dbms.service.IService;

import java.util.List;
import java.util.Map;

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
    public String addRecord(@RequestBody Record record, @PathVariable("databaseTableNames") final String databaseTableNames){
        service.addRecord(record, databaseTableNames);
        return service.findRecordById(record.getKey(), databaseTableNames);
    }

    @RequestMapping(value = "/records/{databaseTableNames}", method = RequestMethod.GET)
    public Map<String, String> getAllRecords(@PathVariable("databaseTableNames") final String databaseTableNames){
        return service.findAllRecords(databaseTableNames);
    }

    @RequestMapping("/records/{databaseTableNames}/{id}")
    public Map<String, String> delete(@PathVariable("databaseTableNames") final String databaseTableNames, @PathVariable("id") final String id) {
        service.deleteRecord(id, databaseTableNames);
        return service.findAllRecords(databaseTableNames);
    }


}
