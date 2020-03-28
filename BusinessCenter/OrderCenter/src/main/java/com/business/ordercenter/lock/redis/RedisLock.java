package com.business.ordercenter.lock.redis;

import com.business.ordercenter.lock.intel.DistributedLock;
import com.business.ordercenter.lock.zookeeper.ZookeeperLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于redis的分布式锁
 */
@Component
@Slf4j
public class RedisLock implements DistributedLock {

    //纳秒和毫秒之间的转换率
    public static final long MILLI_NANO_TIME = 1000 * 1000L;

    public static final String LOCKED = "TRUE";

    //封装的操作redis的工具
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 线程队列，阻塞线程，获取到锁释放，否则一直阻塞
     */
    private Queue<Thread> queue = new ConcurrentLinkedQueue();

    private static final int TIME_SPE = 10;

    private static final String LOCK_PRE = "LOCK_PRE_";

    private Lock lock = new ReentrantLock();

    /**
     * 加锁
     * 使用方式为：
     * lock();
     * try{
     * 	  executeMethod();
     * }finally{
     * 	 unlock();
     * }
     * @param expire 设置锁超时时间
     * @return 成功 or 失败
     */
    public String lock(String key,int expire) throws TimeoutException{
//        log.info("Thread name = " + Thread.currentThread().getName());
        Thread t = Thread.currentThread();
        //放入有序队列
        queue.add(t);
        //记录线程开始获取锁时间
        threadLocal.set(System.currentTimeMillis());
        Boolean result = isTakeLock(LOCK_PRE + key, expire);
        if(result){
            try {
                lock.tryLock((long)expire,TimeUnit.SECONDS);
            } catch (InterruptedException e) {}
            log.info("获取到锁 " + (LOCK_PRE + key) + " id " + t.getName());
            return key;
        }else{
            try {
                Thread.sleep(5);
                lock.tryLock((long)expire,TimeUnit.SECONDS);
                for (; ; ) {
                    result = isTakeLock(key, expire);
                    if (result) {
                        log.info("获取到锁 " + (LOCK_PRE + key) + " id " + t.getName());
                        return key;
                    } else {
                        try {
                            Thread current = Thread.currentThread();
                            Thread.sleep(TIME_SPE);
                            if (System.currentTimeMillis() - threadLocal.get() > expire * 1000) {
                                queue.remove(current);
                                throw new TimeoutException(current.getName());
                            }
                        } catch (InterruptedException e) {
                        }
//                        if (!queue.isEmpty()) {
//                            park(key, expire);
//                        }
                    }
                }
            } catch (InterruptedException e) {
                log.error("",e);
            }
            return key;
        }
    }

    private boolean isTakeLock(String key,int expire){
        return redisTemplate.opsForValue().setIfAbsent(LOCK_PRE + key,"1",expire, TimeUnit.SECONDS);
    }

    private void park(String key,int expire) {
        threadLocal.set(System.currentTimeMillis());
        try {
            Thread t = Thread.currentThread();
            queue.add(t);
            log.info("park id = " + t.getName());
            LockSupport.parkNanos(ZookeeperLock.LOCK_TIMEOUT * expire);
//            if(queue.contains(t)){
//                queue.remove(t);
//                throw new TimeoutException();
//            }
        } catch (Exception e) {
           log.error("park error ",e);
        }
    }

    public boolean unlock(String key) {
        try {
            Thread t = Thread.currentThread();
            double d = (double)(System.currentTimeMillis() - threadLocal.get()) / 1000;
            threadLocal.remove();
            queue.remove(t);
            redisTemplate.delete(LOCK_PRE + key);//直接删除
            boolean is = isTakeLock(LOCK_PRE + key,5);
            log.info(" 锁释放：" + key + "  t = " + t.getName() + "  threadMap size = " + queue.size() + "  time = " + d);
            return true;
        } catch (Exception e) {
            log.error("redis unlock " ,e);
            return false;
        }finally {
            lock.unlock();
        }
    }
}
