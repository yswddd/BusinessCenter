package com.business.ordercenter.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author Zhuming
 * @since 2019-05-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrderList implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id ;

    private String orderId;

    private Integer status;

    private Date createTime;

    private double price;

    private Integer platformType;

    private String productId;

    private String phone;

    private String userId;

}
