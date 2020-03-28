package com.business.api.mode;

import lombok.Data;
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
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = -6473782804625813410L;

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
