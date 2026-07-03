package hoang.com.auction_system_be.service;

import java.util.function.Supplier;


public interface DistributedLockService {
    <T> T executeWithLock(String lockKey, Supplier<T> task);
}
