package com.business.ordercenter.aop;

import com.business.ordercenter.anno.CacheLock;
import com.business.ordercenter.anno.LockedObject;
import com.business.ordercenter.anno.UserId;
import com.business.ordercenter.lock.LockFactory;
import com.business.ordercenter.lock.intel.DistributedLock;
import com.business.ordercenter.lock.util.LockType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

/**
 * 秒杀的aop，这里加了分布式锁
 */
@Component
@Aspect
@Slf4j
public class LockAop {

    @Autowired
    private LockFactory lockFactory;

    @Pointcut("execution(* com.business.ordercenter.business.intel.SeckillService.secKill(String,String))")
    public void pointCut() {
        System.out.println("pointCut");
    }

    /**
     *
     * 属于环绕增强，能控制切点执行前，执行后，，用这个注解后，程序抛异常，会影响@AfterThrowing这个注解
     *
     * org.aspectj.lang.JoinPoint 接口表示目标类连接点对象，它定义这些主要方法。
     * Object[] getArgs()：获取连接点方法运行时的入参列表。
     * Signature getSignature()：获取连接点的方法签名对象。
     * Object getTarget()：获取连接点所在的目标对象。
     * Object getThis()：获取代理对象。
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around(value="pointCut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable{
        long t = System.currentTimeMillis();
        DistributedLock dlock = lockFactory.getLock(LockType.REDIS);
        Object obj = null;
        //加锁
        Object userId = getArg(pjp, UserId.class);
        Object lockedObject = getArg(pjp, LockedObject.class);
        String key = userId + "|" + lockedObject.toString();
        CacheLock cacheLock = getAnnotation(pjp, CacheLock.class);
        //加锁成功，执行方法
        try{
            //redis 分布式锁
//            redisQueue.lock(key,lockedObject.toString(),cacheLock.expireTime());

            //zk 分布式锁
            String lock = dlock.lock(lockedObject.toString(),cacheLock.expireTime());

            long t1 = System.currentTimeMillis();
            double lastTime = (double)(t1 - t) / 1000;
            log.info("上锁时间" + lastTime + " lock key = " + dlock);

            Object[] args = pjp.getArgs();
            obj = pjp.proceed(args);

            long t2 = System.currentTimeMillis();
            lastTime = (double)(t2 - t1) / 1000;
            log.info("处理时间" + lastTime);
            dlock.unlock(lock);
            long t3 = System.currentTimeMillis();
            lastTime = (double)(t3 - t2) / 1000;
            log.info("放锁时间" + lastTime);
        }catch (TimeoutException ex){
            dlock.unlock(ex.getMessage());
            return -2;
        }
        return obj;
    }

    /**
     * 获取方法的注解
     * @param joinPoint
     * @param clazz
     * @return
     */
    public <T extends Annotation>T getAnnotation(JoinPoint joinPoint, Class<T> clazz){
        try{
            //获取接口上的注解
            for (Class<?> cls : joinPoint.getTarget().getClass().getInterfaces()) {
                Method[] methods = cls.getMethods();
                for(Method method : methods){
                    //获取方法中的注解
                    T t = method.getAnnotation(clazz);
                    if (t != null) {
                        return t;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取参数的注解
     * @param joinPoint
     * @param clazz
     * @return
     */
    public Object getArg(JoinPoint joinPoint, Class<?> clazz){
        try{
            ;
            //获取接口上的注解
            for (Class<?> cls : joinPoint.getTarget().getClass().getInterfaces()) {
                Method[] methods = cls.getMethods();
                for(Method method : methods){
                    //获得方法中参数的注解
                    Annotation[][] annotations = method.getParameterAnnotations();
                    for(int i = 0;i < annotations.length;i++){
                        for(int j = 0;j < annotations[i].length;j++) {
                            if (annotations[i][j].annotationType().equals(clazz)) {
                                return joinPoint.getArgs()[i];
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
