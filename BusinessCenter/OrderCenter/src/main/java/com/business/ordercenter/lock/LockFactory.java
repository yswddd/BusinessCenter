package com.business.ordercenter.lock;

import com.business.ordercenter.lock.intel.DistributedLock;
import com.business.ordercenter.lock.redis.RedisLock;
import com.business.ordercenter.lock.util.LockType;
import com.business.ordercenter.lock.zookeeper.ZookeeperLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LockFactory {

    @Autowired
    private ZookeeperLock zookeeperLock;

    @Autowired
    private RedisLock redisLock;

    public DistributedLock getLock(LockType type){
        switch (type) {
            case REDIS:
                return redisLock;
            case ZOOKEEPER:
                return zookeeperLock;
            case MYSQL:
                return null;
        }
        return null;
    }
}
