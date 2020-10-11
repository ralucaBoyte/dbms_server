package service;

import domain.Database;
import repository.IRepository;

public class Service {
    private IRepository repository;

    public Service(IRepository repository) {
        this.repository = repository;
    }

    public Database addDatabase(Database database){
        return repository.addDatabase(database);
    }
}
