package com.business.ordercenter.business.impl;

import com.business.ordercenter.business.intel.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀扣库存
 */
@Slf4j
@Component
public class SecKillServiceImpl implements SeckillService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public int secKill(String userId, String productId) {
        //首先判断是否扣库存成功
        int size = productReduce(productId);
        return size;
    }

    /**
     * 减库存
     * @param productId
     * @return 剩余库存
     */
    public int productReduce(String productId){
        try{
            int size = Integer.parseInt(redisTemplate.opsForValue().get(productId).toString());
//            System.out.println("^^^^^^^   减库存 rest size = " + size);
            if(size <= 0){
                return -1;
            }else{
                redisTemplate.opsForValue().set(productId,String.valueOf(--size), 12,TimeUnit.HOURS);
            }
//        System.out.println("~~~~~~~~~~   commidityID:" + productId + " rest size = " + size + "   ~~~~~~~~~~~");
            return size;
        }catch (Exception e){
            log.error("productReduce error productId = " + productId ,e);
        }
        return 0;
    }
}
