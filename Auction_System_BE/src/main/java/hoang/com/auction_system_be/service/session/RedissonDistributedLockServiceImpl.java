package hoang.com.auction_system_be.service.impl;

import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.service.DistributedLockService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@ConditionalOnProperty(name = "app.lock.type", havingValue = "redis", matchIfMissing = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RedissonDistributedLockServiceImpl implements DistributedLockService {

    RedissonClient redissonClient;

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> task) {
        // Initialize RLock based on the key (Example: "item:123")
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            // 1. Attempt to acquire the lock (tryLock)
            // waitTime (5s): Maximum time this thread will wait to acquire the lock if someone else is holding it.
            // leaseTime (10s): Maximum time the lock is held. After 10s, it automatically releases to prevent Deadlock if the server crashes.
            isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);

            if (!isLocked) {
                log.warn("Lock is currently held by another process. Cannot access lockKey: {}", lockKey);
                // System is overloaded or highly contested -> Throw an error immediately to release the Request
                throw new AppException(ErrorCode.SYSTEM_BUSY); 
            }

            // 2. Lock successfully acquired, execute Business Logic (TransactionTemplate is inside here)
            return task.get();

        } catch (InterruptedException e) {
            // Restore the interrupted status of the Thread
            Thread.currentThread().interrupt();
            log.error("Process was abruptly interrupted while waiting for lock: {}", lockKey, e);
            throw new AppException(ErrorCode.SYSTEM_ERROR);
        } finally {
            // 3. Always release the lock in the finally block
            // Must verify that this exact thread is holding the lock before releasing it, to avoid accidentally releasing another thread's lock (due to leaseTime expiration)
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
