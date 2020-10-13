package dbms.controller;

import dbms.domain.Attribute;
import dbms.domain.Database;
import dbms.domain.Index;
import dbms.domain.Table;
import dbms.dto.DatabaseTableDTO;
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

    @RequestMapping(value = "/databases", method = RequestMethod.POST)
    public Database addDatabase(@RequestBody Database database) {
        Database newDatabase = service.addDatabase(database);
        return newDatabase;
    }

    @RequestMapping(value = "/databases/{databaseName}", method = RequestMethod.DELETE)
    public Database removeDatabase(@PathVariable String databaseName) {
        Database newDatabase = service.removeDatabase(databaseName);
        return newDatabase;
    }

    @RequestMapping(value = "/databases", method = RequestMethod.GET)
    public List<Database> getAllDatabases() {
        return service.getAllDatabases();
    }

    @RequestMapping(value = "/tables", method = RequestMethod.POST)
    public DatabaseTableDTO addTable(@RequestBody DatabaseTableDTO databaseTableDTO){
        DatabaseTableDTO newDatabaseTableDTO = service.addTable(databaseTableDTO.getDatabaseName(), databaseTableDTO.getTable());
        return newDatabaseTableDTO;
    }

    @RequestMapping(value = "/databases/{databaseName}/tables/{tableName}", method = RequestMethod.DELETE)
    public DatabaseTableDTO removeTable(@PathVariable String databaseName, @PathVariable String tableName){
        Table table = service.removeTable(databaseName, tableName);
        return new DatabaseTableDTO(databaseName, table);
    }

    @RequestMapping(value = "/index/database/{databaseName}/table/{tableName}", method = RequestMethod.POST)
    public Index addIndex(@RequestBody List<Attribute> attributeList, @PathVariable String databaseName, @PathVariable String tableName){
        return service.addIndex(attributeList, databaseName, tableName);
    }


}
