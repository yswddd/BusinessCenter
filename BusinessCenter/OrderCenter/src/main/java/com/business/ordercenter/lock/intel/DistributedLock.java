package com.business.ordercenter.lock.intel;

import java.util.concurrent.TimeoutException;

public interface DistributedLock {

    public String lock(String key, int expire) throws TimeoutException;

    public boolean unlock(String lock);

}
