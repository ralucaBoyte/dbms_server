package dbms;

import dbms.domain.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dbms.repository.Repository;
import dbms.service.Service;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class AppConfig {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    @Bean(name= "dbms/service")
    public Service createService(){ return new Service();}

    @Bean(name= "dbms/repository")
    public Repository createRepository(){ return new Repository(redisTemplate);}


}
