package com.business.db.dubbo;

import com.business.api.mode.OrderDetail;
import com.business.api.mode.ResponseModel;
import com.business.api.service.OrderInfoService;
import com.business.db.dao.opt.OrderInsertHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * dubbo实现类，传输order对象
 */
@Service(version = "1.0.0")
@Slf4j
public class OrderInfoServiceImpl implements OrderInfoService{

    @Autowired
    private OrderInsertHandler orderInsertHandler;

    @Autowired
    private OrderInsertHandler orderUpdateHandler;

    /**
     * 获取订单，新增
     * @param detail
     * @return
     */
    public ResponseModel insertOrderDetail(OrderDetail detail) {
        try{
            orderInsertHandler.addQueue(detail);
            return new ResponseModel(true,"");
        }catch (Exception e){
            log.error("insert into db error " ,e);
            return new ResponseModel(false,"insertOrderDetail error");
        }
    }

    /**
     * 获取订单，update
     * @param detail
     * @return
     */
    public ResponseModel updateOrderDetail(OrderDetail detail) {
        try{
            orderUpdateHandler.addQueue(detail);
            return new ResponseModel(true,"");
        }catch (Exception e){
            log.error("insert into db error " ,e);
            return new ResponseModel(false,"updateOrderDetail error");
        }
    }
}
