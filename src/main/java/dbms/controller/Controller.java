package dbms.controller;

import dbms.domain.Database;
import dbms.dto.DatabaseTableDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import dbms.service.IService;

@RestController
@RequestMapping("/dbms")
public class Controller {
    @Autowired
    private IService service;

    @RequestMapping(value = "/databases", method = RequestMethod.POST)
    public Database addDatabase(@RequestBody Database database) {
        Database newDatabase =service.addDatabase(database);
        return newDatabase;
    }

    public DatabaseTableDTO addTable(@RequestBody DatabaseTableDTO databaseTableDTO){
        DatabaseTableDTO newDatabaseTableDTO = service.addTable(databaseTableDTO.getDatabaseName(), databaseTableDTO.getTable());
        return newDatabaseTableDTO;
    }
}
