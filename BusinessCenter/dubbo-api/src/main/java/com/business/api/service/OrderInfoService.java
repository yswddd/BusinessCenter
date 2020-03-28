package com.business.api.service;

import com.business.api.mode.OrderDetail;
import com.business.api.mode.ResponseModel;

/**
 * 传输订单信息
 */
public interface OrderInfoService {

    ResponseModel insertOrderDetail(OrderDetail detail);

    ResponseModel updateOrderDetail(OrderDetail detail);
}
