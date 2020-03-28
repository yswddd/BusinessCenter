package com.business.ordercenter.business.intel;

import com.business.api.mode.OrderDetail;
import com.business.ordercenter.model.OrderResponse;

/**
 * 下订单接口
 */
public interface OrderService {

    public OrderResponse takeOrder(OrderDetail lockRequest);
}
