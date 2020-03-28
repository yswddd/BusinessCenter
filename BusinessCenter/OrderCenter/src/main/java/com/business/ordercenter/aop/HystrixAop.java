package com.business.ordercenter.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.*;

/**
 * hysteixAop，这里加了超时
 */
@Component
@Aspect
@Slf4j
public class HystrixAop {

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Pointcut("execution(* com.business.ordercenter.controller.OrderController.hystrixTest(String))")
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
        //加锁成功，执行方法
        try{
            MyTask myTask = new MyTask(pjp);//实例化任务，传递参数
            FutureTask<Object> futureTask = new FutureTask<>(myTask);//将任务放进FutureTask里
            threadPoolExecutor.execute(futureTask);
            Object args = futureTask.get(5, TimeUnit.SECONDS);
            return args;
        }catch (TimeoutException ex){
            log.error("doAround error ",ex);
            return null;
        }
    }

    public class MyTask implements Callable<Object> {
        private ProceedingJoinPoint pjp;
        //构造函数，用来向task中传递任务的参数
        public  MyTask(ProceedingJoinPoint pjp) {
            this.pjp = pjp;
        }
        //任务执行的动作
        @Override
        public Object call() {
            try{
                Object[] args = pjp.getArgs();
                return pjp.proceed(args);
            }catch (Throwable e){
                log.error("call error ",e);
            }
            return null;
        }
    }

}
