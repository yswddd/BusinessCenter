package com.business.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.business.api.mode.OrderDetail;
import com.business.db.dao.mapper.OrderMapper;
import com.business.db.dao.service.OrderSketchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderSketchServiceImpl extends ServiceImpl<OrderMapper, OrderDetail> implements OrderSketchService {
}
