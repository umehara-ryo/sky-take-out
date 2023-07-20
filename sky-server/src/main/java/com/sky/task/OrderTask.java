package com.sky.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {


    @Autowired
    private OrderMapper orderMapper;

    //处理超时订单
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder(){
        log.info("定时处理超时订单{}", LocalDateTime.now());

        LocalDateTime outTime = LocalDateTime.now().minusMinutes(15);
        //查询超过15分钟并且处于待付款的order
        List<Orders> ordersList = orderMapper.getTimeoutOrderByStatus(Orders.PENDING_PAYMENT,outTime);

        if(ordersList != null && ordersList.size() > 0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason(MessageConstant.ORDER_TIME_OUT);
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    //处理派送中订单
    @Scheduled(cron = "0 0 4 * * ? ")
    public void processDeliveryOrder(){
        log.info("处理派送中的订单",LocalDateTime.now());


        LocalDateTime outTime = LocalDateTime.now().minusHours(4);
        //查询处于当前时间4小时前并且处于配送中的order
        List<Orders> ordersList = orderMapper.getTimeoutOrderByStatus(Orders.DELIVERY_IN_PROGRESS,outTime);

        if(ordersList != null && ordersList.size() > 0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }

}
