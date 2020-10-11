package dbms.service;


import dbms.domain.Database;
import dbms.repository.IRepository;
import org.springframework.beans.factory.annotation.Autowired;


public class Service implements IService{
    @Autowired
    private IRepository repository;

    public Service(){};

    public Database addDatabase(Database database){
        return repository.addDatabase(database);
    }
}
