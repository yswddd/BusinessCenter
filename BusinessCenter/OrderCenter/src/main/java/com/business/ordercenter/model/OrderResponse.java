package com.business.ordercenter.model;

public class OrderResponse extends BaseResponse {
    public Order order = new Order();

    public final static class Order{
        public int restCount;
        public String orderId;
    }
}
