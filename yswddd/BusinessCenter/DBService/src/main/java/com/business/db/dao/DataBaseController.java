package com.business.db.dao;

import com.alibaba.fastjson.JSONObject;
import com.business.api.mode.OrderDetail;
import com.business.api.util.Constants;
import com.business.db.dao.service.OrderSketchService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 数据库数据对列，这里负责接收，从mq获取到的数据，对应的入库然后删除redis
 */
@Slf4j
@Component
public class DataBaseController implements Runnable{

    /**
     * 数据库处理10s还未入库的请求
     */
    public final static long DATEBASE_SAVE_TIMEOUT = 10;

    @Autowired
    private OrderSketchService orderSketchService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

//    @PostConstruct
//    public void initThread(){
//        new Thread(this,"DataBaseController-001").start();
//    }

    @RabbitListener(queues = "topic.message")
    public void getMessage(Message message, Channel channel) {
        try{
            OrderDetail orderSketch = JSONObject.parseObject(message.getBody(), OrderDetail.class);
            saveDB(orderSketch);
        }catch (Exception e){
            log.error("insert into db error " ,e);
        }
    }

    private void saveDB(OrderDetail orderList){
        //操作数据库
        orderSketchService.save(orderList);
        redisTemplate.opsForHash().delete(Constants.DATEBASE_KEY_INSERT,orderList.getOrderId());
    }

    /**
     * 20秒钟执行一次,从redis取出要插入数据库的字段，如果有就插入并删除redis
     */
    @Scheduled(cron = "0/20 * * * * ?")
    public void run() {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(Constants.DATEBASE_KEY_INSERT);
            if (entries == null || entries.size() == 0) {
                log.info(" run ................");
                return;
            }
            entries.forEach((key, value) -> {
                OrderDetail orderSketch = JSONObject.parseObject(value.toString(), OrderDetail.class);
                if (orderSketch != null) {
                    try {
                        long sec = (System.currentTimeMillis() - orderSketch.getCreateTime().getTime()) / 1000;
                        if (sec >= DATEBASE_SAVE_TIMEOUT) {
                            log.info(key + "：" + value);
                            saveDB(orderSketch);
                            //删除缓存
                            redisTemplate.opsForHash().delete(Constants.DATEBASE_KEY_INSERT,key);
                        }
                    } catch (Exception ex) {
                        log.error("entries run error ", ex);
                        //删除缓存
                        redisTemplate.opsForHash().delete(Constants.DATEBASE_KEY_INSERT,key);
                    }
                }
            });
        } catch (Exception e) {
            log.error("DataBaseController run error ", e);
        }
    }
}
