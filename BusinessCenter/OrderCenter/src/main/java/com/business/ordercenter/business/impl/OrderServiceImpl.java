package com.business.ordercenter.business.impl;

import com.alibaba.fastjson.JSONObject;
import com.business.api.mode.OrderDetail;
import com.business.api.service.OrderInfoService;
import com.business.ordercenter.business.intel.OrderService;
import com.business.ordercenter.business.intel.SeckillService;
import com.business.ordercenter.model.OrderResponse;
import com.business.ordercenter.util.OrderIdGenerator;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * 下单impl，首先获取库存，没有库存就失败
 */
@Component
public class OrderServiceImpl implements OrderService {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private OrderIdGenerator orderIdGenerator;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Reference(version = "1.0.0", check = false,retries = 0)
    private OrderInfoService orderInfoService;


    /**
     * 秒杀
     * @param orderSketch
     * @return
     */
    public OrderResponse takeOrder(OrderDetail orderSketch){
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.userId = orderSketch.getUserId() + "";
        //首先判断是否扣库存成功
        int type = seckillService.secKill(orderSketch.getUserId() + "",orderSketch.getProductId() + "");
        if(type == -2){
            orderResponse.responseCode = "2";
            orderResponse.responseMsg = "请求超时，请重试！";
        }else if(type == -1){
            orderResponse.responseCode = "1";
            orderResponse.responseMsg = "没有库存！";
        }else{//然后下单入库
            //获取订单号
            String orderId = orderIdGenerator.generatorOrderId(orderSketch.getUserId());
            //入库
//            OrderSketch orderSketch = new OrderSketch();
//            orderSketch.setOrderId(orderId);
//            orderSketch.setCreateTime(new Date());
//            orderSketch.setUserId(userId);

            //操作数据库
            orderResponse.order.orderId = orderId;
            orderResponse.responseCode = "0";
            orderResponse.responseMsg = "下单成功！";
            orderResponse.order.restCount = type;
            orderSketch.setOrderId(orderId);
            orderSketch.setCreateTime(new Date());
            String jsonString = JSONObject.toJSONString(orderSketch);

            //dubbo发送订单数据
            orderInfoService.insertOrderDetail(orderSketch);

//            redisTemplate.opsForHash().put(Constants.DATEBASE_KEY_INSERT,orderId,jsonString);
//            amqpTemplate.convertAndSend("topic.message", jsonString);
        }
        return orderResponse;
    }

    /**
     * 秒杀
     * @param userId
     * @param productId
     * @return
     */
    /*public OrderResponse takeOrder(String userId, String productId){
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.userId = userId;
        //首先判断是否扣库存成功
        int type = seckillService.secKill(userId,productId);
        if(type == -2){
            orderResponse.responseCode = "2";
            orderResponse.responseMsg = "请求超时，请重试！";
        }else if(type == -1){
            orderResponse.responseCode = "1";
            orderResponse.responseMsg = "没有库存！";
        }else{//然后下单入库
            //获取订单号
            String orderId = orderIdGenerator.generatorOrderId(userId);
            //入库
            OrderSketch orderSketch = new OrderSketch();
//            orderSketch.setOrderId(orderId);
//            orderSketch.setCreateTime(new Date());
//            orderSketch.setUserId(userId);

            //操作数据库

            orderResponse.order.orderId = orderId;
            orderResponse.responseCode = "0";
            orderResponse.responseMsg = "下单成功！";
            orderResponse.order.restCount = type;
            String jsonString = JSONObject.toJSONString(orderSketch);
            redisTemplate.opsForHash().put(Constants.DATEBASE_KEY,orderId,jsonString);
            amqpTemplate.convertAndSend("topic.message", jsonString);
        }
        return orderResponse;
    }*/
}
