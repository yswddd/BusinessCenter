package com.business.db.dao.opt;

import com.business.api.mode.OrderDetail;
import com.business.api.mode.ResponseModel;
import com.business.api.util.Constants;
import com.business.db.dao.service.OrderSketchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Component
@Slf4j
public class OrderUpdateHandler extends AbstractOrderHandler<OrderDetail>{

    @Autowired
    private OrderSketchService orderSketchService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取订单，修改
     * @param detail
     * @return
     */
    public void updateOrSaveDB(OrderDetail detail) {
        try{
            if(detail.getOrderId() == null){
                throw new NullPointerException();
            }
            //操作数据库
            orderSketchService.updateById(detail);
        }catch (Exception e){
            log.error("insert into db error " ,e);
        }
    }
}
