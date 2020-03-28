package com.business.db.dao.opt;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
public abstract class AbstractOrderHandler<T> implements Runnable{

    private BlockingQueue<T> queue = new LinkedBlockingDeque();
    private boolean isStop = false;

    @PostConstruct
    public void initThread(){
        new Thread(this).start();
    }

    public void addQueue(T t){
        queue.add(t);
        System.out.println(t.getClass());
    }

    public void stopThread(){
        isStop = true;
    }

    @Override
    public void run() {
        try {
            while (!isStop){
                T detail = queue.take();
                updateOrSaveDB(detail);
            }
        }catch (Exception e){
            log.error("",e);
        }
    }

    protected abstract void updateOrSaveDB(T t);
}
