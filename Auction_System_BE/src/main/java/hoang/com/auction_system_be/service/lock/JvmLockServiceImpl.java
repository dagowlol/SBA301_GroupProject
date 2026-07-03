package hoang.com.auction_system_be.service.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import hoang.com.auction_system_be.service.DistributedLockService;

@Service
@ConditionalOnProperty(name = "app.lock.type", havingValue = "jvm")
public class JvmLockServiceImpl implements DistributedLockService {

    private static final ConcurrentHashMap<String, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> task) {
        ReentrantLock lock = LOCKS.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
        lock.lock();
        try {
            return task.get();
        } finally {
            lock.unlock();
        }
    }
}