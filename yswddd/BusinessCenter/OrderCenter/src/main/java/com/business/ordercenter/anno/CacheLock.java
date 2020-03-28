package com.business.ordercenter.anno;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheLock {
    String lockedPrefix() default "";//redis 锁key的前缀
    long timeOut() default 5;//轮询锁的时间
    int expireTime() default 5;//key在redis里存在的时间，1000S
}
