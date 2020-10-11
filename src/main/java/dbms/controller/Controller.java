package dbms.controller;

import dbms.domain.Database;
import dbms.domain.Table;
import dbms.dto.DatabaseTableDTO;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import dbms.service.IService;

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

    @RequestMapping(value = "/databases", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, method = RequestMethod.DELETE)
    public Database removeDatabase(@RequestParam("databaseName") String databaseName) {
        Database newDatabase = service.removeDatabase(databaseName);
        return newDatabase;
    }

    @RequestMapping(value = "/tables", method = RequestMethod.POST)
    public DatabaseTableDTO addTable(@RequestBody DatabaseTableDTO databaseTableDTO){
        DatabaseTableDTO newDatabaseTableDTO = service.addTable(databaseTableDTO.getDatabaseName(), databaseTableDTO.getTable());
        return newDatabaseTableDTO;
    }

    @RequestMapping(value = "/tables", method = RequestMethod.DELETE)
    public Table addTable(@RequestParam("databaseName") String databaseName, @RequestParam("tableName") String tableName){
        return service.removeTable(databaseName, tableName);
    }
}
