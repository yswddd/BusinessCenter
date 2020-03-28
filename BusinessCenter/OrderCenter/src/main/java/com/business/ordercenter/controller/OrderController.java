package com.business.ordercenter.controller;

import com.business.api.mode.OrderDetail;
import com.business.api.util.Constants;
import com.business.ordercenter.business.intel.OrderService;
import com.business.ordercenter.model.OrderResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author WAF
 * @date 2019/6/18
 * 订单controller，下单接口
 */
@RestController
@RequestMapping("/order")
@AllArgsConstructor
@Slf4j
public class OrderController {

    private OrderService orderService;

    private RedisTemplate<String, Object> redisTemplate;

    private ZooKeeper zkClient;

    @PostMapping("/take")
    public OrderResponse takeOrder(@RequestBody OrderDetail lockRequest) {

        return orderService.takeOrder(lockRequest);

    }

//    @PostMapping("/kill")
//    public OrderResponse secKill(@RequestBody OrderRequest lockRequest) {
//        return orderService.takeOrder(lockRequest.userId,lockRequest.productId);
//    }

    @GetMapping("/init")
    public String initRedis(String inputParameter) {
        redisTemplate.opsForValue().set("P_101","10000");
        redisTemplate.opsForValue().set("P_102","10000");
        System.out.println("101 rest size = " + redisTemplate.opsForValue().get("P_101"));
        System.out.println("102 rest size = " + redisTemplate.opsForValue().get("P_102"));
        return "10000";
    }

    @GetMapping("/size")
    public String getSize(String inputParameter) {
        System.out.println("101 rest size = " + redisTemplate.opsForValue().get("P_101"));
        System.out.println("102 rest size = " + redisTemplate.opsForValue().get("P_102"));
        return "size";
    }

    @PostMapping("/clear")
    public OrderResponse clear(String inputParameter) {
        OrderResponse o = new OrderResponse();
        o.responseCode = "0";
        redisTemplate.opsForValue().set("P_101","10000");
        redisTemplate.opsForValue().set("P_102","10000");
        return o;
    }

    @GetMapping("/clearhtml")
    public OrderResponse clearHtml(String inputParameter) {
        OrderResponse o = new OrderResponse();
        o.responseCode = "1";
        redisTemplate.opsForValue().set("P_101","10000000");
        redisTemplate.opsForValue().set("P_102","10000000");
        return o;
    }

    @PostMapping("/demo")
    public String demo(String inputParameter) {
//        demo.testMq();
        return "testMq";
    }

    @GetMapping("/children")
    public String testLock(String inputParameter) {
        try{
            List<String> childrenNodes = zkClient.getChildren(Constants.LOCK_ROOT_PATH,true);
            childrenNodes.forEach((node)-> System.out.println(node));
        }catch (Exception e){

        }
        return "testMq";
    }

    //127.0.0.1:9000/order/hystrix
    @GetMapping("/hystrix")
    public  String hystrixTest(String inputParameter) {
        try{
            TimeUnit.SECONDS.sleep(10);
            System.out.println("@@@@@@@@@@@@@@@@@@@@");
        }catch (Exception e){
            log.error("",e);
        }
        return "testMq";
    }

    @GetMapping("/thread")
    public String threadDemo(String inputParameter) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(100,
                200, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolExecutor.execute(new Runnable() {
            public void run() {
                System.out.println("ddddddd");
            }
        });
        return "testMq";
    }



//    @RabbitListener(queues = "queue")
//    public void getMes(String text){
//        System.out.println("getMes = " + text);
//    }
}
