package com.business.ordercenter.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 订单号唯一生成器（多节点）
 * 生成规则 时间戳 + userId hash + 随机4位数
 * 每次生成到redis对比如果重复了继续重新生成，重新生成只改变随机数
 */
@Component
public class OrderIdGenerator {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    private final static int LENGTH = 3;

    //封装的操作redis的工具
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public String generatorOrderId(long userId){
        return generatorOrderId(Long.toString(userId));
    }

    public String generatorOrderId(String userId){
        StringBuilder builder = new StringBuilder(getCurrentTime());
        StringBuilder b = new StringBuilder();
        b.append(getCurrentTime());
        b.append(getHashUserId(userId));
        b.append(getRadom());
        //判断是否订单号已经存在了，已经存在了就更换
        while(!lock(b.toString(),2)){
            b.delete(b.length() - 4,b.length() - 1);
            b.append(getRadom());
        }
//        System.out.println("### OrderId = " + b.toString() + " length = " + b.length());
        return b.toString();
    }

    private String getCurrentTime(){
        return sdf.format(new Date());
    }

    private String getHashUserId(String userId){
        int code = userId.hashCode();
        int hash = code % 1000;
        return matchString(String.valueOf(hash),LENGTH);
    }

    private String matchString(String code,int size){
        StringBuilder b = new StringBuilder();
        for (int i = 3 - code.length(); i > 0;i--){
            b.append("0");
        }
        b.append(code);
        return b.toString();
    }

    private String getRadom(){
        Random r = new Random();
        //获取0-999之间的随机数
        int number = r.nextInt(1000);
        return matchString(String.valueOf(number),LENGTH);
    }

    public boolean lock(String key,int expire){
//        redisTemplate.delete(key);//直接删除
//        Boolean result = redisTemplate.opsForValue().setIfAbsent(key.trim(),"");
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key,"1",expire, TimeUnit.SECONDS);
//        try {
//            TimeUnit.SECONDS.sleep(1);
//            result = redisTemplate.opsForValue().setIfAbsent(key,key,expire, TimeUnit.SECONDS);
//        } catch (Throwable e) {
//
//        }
//        System.out.println(" lock key = " + key + "  result = " + result);
        return result;
    }
}
