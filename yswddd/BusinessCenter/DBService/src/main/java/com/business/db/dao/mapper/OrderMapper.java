package com.business.db.dao.mapper;

import com.business.api.mode.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Zhuming
 * @since 2019-05-28
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderDetail> {

    OrderDetail selectByOrderId(String orderId);
}
