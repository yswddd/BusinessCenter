package com.business.ordercenter.lock.zookeeper;

import com.business.api.util.Constants;
import com.business.ordercenter.lock.intel.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

/**
 * 基于zk的分布式锁
 */
@Slf4j
@Component
public class ZookeeperLock implements DistributedLock {

    private ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static final long LOCK_TIMEOUT = 1000 * 1000 * 1000;
    /**
     * 线程Map
     */
    private Map<String, Thread> threadMap = new ConcurrentHashMap<>();

    @Autowired
    private ZooKeeper zkClient;

    public String lock(String key,int expire) throws TimeoutException{
        //记录线程开始获取锁时间
        threadLocal.set(System.currentTimeMillis());
        return tryLock(key,expire);
    }

    private String tryLock(String key,int expire) throws TimeoutException{
        try{
            // 创建EPHEMERAL_SEQUENTIAL类型节点
            String lockPath = zkClient.create(Constants.LOCK_ROOT_PATH + "/" + key + Constants.LOCK_NODE_SPE,
                    Thread.currentThread().getName().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);
//            log.info("锁创建成功 lockPath = " + lockPath);
            //获取子节点
            List<String> ChildrenNodes = zkClient.getChildren(Constants.LOCK_ROOT_PATH,true);
            SortedSet<String> sortedSet = new TreeSet<>();
            ChildrenNodes.forEach((children)->sortedSet.add(Constants.LOCK_ROOT_PATH + "/" + children));
//            sortedSet.forEach((a)-> System.out.println("ChildrenNodes " + a));
            //拿到最小的节点
            String first = sortedSet.first();
            if(lockPath.equals(first)){
                //当前就是最小节点
                log.info("获取到锁 id = " + lockPath);
                return lockPath;
            }else{
                //阻塞线程直到前一个锁释放
                SortedSet<String> lessThanLockId = sortedSet.headSet(lockPath);
                if(!lessThanLockId.isEmpty()){
                    String preId = lessThanLockId.last();
//                    log.info("阻塞当前线程 " + lockPath + " 监听 pre path = " + preId);
                    Stat preStat = zkClient.exists(preId,(event)->{
                        //当前节点是否删除
                        if(event.getType()== Watcher.Event.EventType.NodeDeleted) {
                            Thread currentThread = threadMap.get(event.getPath());
                            if(currentThread != null){
                                threadMap.remove(event.getPath());
                                LockSupport.unpark(currentThread);
//                                log.info("*********** 锁删除 ***********" + event.getPath() );
                            }else{
//                                log.info("*********** 锁删除 map 找不到***********" + event.getPath() );
                            }
                        }
                    });
                    if(preStat == null){
                        return lockPath;
                    }
                    Thread currentThread = Thread.currentThread();
                    threadMap.put(preId,currentThread);
                    LockSupport.parkNanos(LOCK_TIMEOUT * expire);
                    if(threadMap.get(preId) != null){
                        log.info("@@@@@@ 超时 " + preId);
                        throw new TimeoutException(lockPath);
                    }
                    return lockPath;
                }
            }
        }catch (TimeoutException e){
            throw new TimeoutException(e.getMessage());
        }
        catch (Exception e){
            log.error("ZookeeperLock error ",e);
        }
        return null;
    }

    public boolean unlock(String lock){
        try{
            //打印获取锁到释放耗时
            long ttt = System.currentTimeMillis();
            double lastTime = (double)(ttt - threadLocal.get()) / 1000;
            log.info(" 锁释放：" + lock + "  threadMap size = " + threadMap.size() + " spent time = " + lastTime);
            zkClient.delete(lock, -1);
            return true;
        }catch (KeeperException e){
            log.error("ZookeeperLock unLock error ",e);
        }catch (InterruptedException e){
            log.error("ZookeeperLock unLock error ",e);
        }
        return false;
    }

}
