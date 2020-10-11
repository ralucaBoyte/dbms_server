package dbms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dbms.repository.Repository;
import dbms.service.Service;

@Configuration
public class AppConfig {

    @Bean(name= "dbms/service")
    public Service createService(){ return new Service();}

    @Bean(name= "dbms/repository")
    public Repository createRepository(){ return new Repository();}


}
