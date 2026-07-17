package hoang.com.auction_system_be.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a simple in-memory CacheManager.
 * In production with Redis, replace this with RedisCacheManager
 * by removing the Redis auto-config exclusion and this class.
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("auction_session_detail", "statisticsCache");
    }
}
