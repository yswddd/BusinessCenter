package com.business.ordercenter.business.intel;

import com.business.ordercenter.anno.CacheLock;
import com.business.ordercenter.anno.LockedObject;
import com.business.ordercenter.anno.UserId;

public interface SeckillService {
    /**
     *现在暂时只支持在接口方法上注解
     * cacheLock注解可能产生并发的方法
     * 最简单的秒杀方法，参数是用户ID和商品ID。可能有多个线程争抢一个商品，所以商品ID加上LockedObject注解
     */
    @CacheLock(lockedPrefix="TEST_PREFIX")
    public int secKill(@UserId String userID, @LockedObject String productId);

}
