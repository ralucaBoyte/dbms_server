import domain.Database;
import repository.Repository;
import service.Service;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Database database = new Database("d2", null);
        Repository repository = new Repository();
        Service service = new Service(repository);
        service.addDatabase(database);
    }
}
