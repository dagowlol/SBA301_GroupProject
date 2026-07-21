package hoang.com.auction_system_be.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.lock.type", havingValue = "redis")
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:#{null}}")
    private String redisPassword;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisUrl = String.format("redis://%s:%d", redisHost, redisPort);

        // Configuration for Single Server (Suitable for most systems today)
        // If switching to Redis Cluster in the future, simply change useSingleServer() to useClusterServers()
        config.useSingleServer()
              .setAddress(redisUrl)
              .setConnectionMinimumIdleSize(10) // Keep 10 connections ready to respond quickly
              .setConnectionPoolSize(64)        // Maximum of 64 simultaneous connections
              .setIdleConnectionTimeout(10000)
              .setConnectTimeout(10000)
              .setTimeout(3000);                // Timeout when waiting for Redis response

        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}
