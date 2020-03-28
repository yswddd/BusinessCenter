package com.business.db.dao.opt;

import com.business.api.mode.OrderDetail;
import com.business.api.util.Constants;
import com.business.db.dao.service.OrderSketchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderInsertHandler extends AbstractOrderHandler<OrderDetail>{

    @Autowired
    private OrderSketchService orderSketchService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    protected void updateOrSaveDB(OrderDetail orderList){
        //操作数据库
        orderSketchService.save(orderList);
        redisTemplate.opsForHash().delete(Constants.DATEBASE_KEY_INSERT,orderList.getOrderId());
    }
}
