package dbms.controller;

import dbms.domain.Attribute;
import dbms.domain.Database;
import dbms.domain.Index;
import dbms.domain.Table;
import dbms.dto.DatabaseTableDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import dbms.service.IService;

import java.util.List;

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
    public Table removeTable(@PathVariable String databaseName, @PathVariable String tableName){
        Table tableToBeRemoved = service.removeTable(databaseName, tableName);
        logger.info("++++++++++++++++++Removed table++++++++++++++++++", tableToBeRemoved);
        return tableToBeRemoved;
    }

    @RequestMapping(value = "/index/database/{databaseName}/table/{tableName}", method = RequestMethod.POST)
    public Index addIndex(@RequestBody List<Attribute> attributeList, @PathVariable String databaseName, @PathVariable String tableName){
        return service.addIndex(attributeList, databaseName, tableName);
    }


}
