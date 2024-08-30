package com.my.task;

import com.my.entity.Orders;
import com.my.mapper.OrderMapper;
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

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单 {}", LocalDateTime.now());

        List<Orders> ordersList = orderMapper.getByStatusOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));

        if(!ordersList.isEmpty()) {
            for (Orders order : ordersList) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("订单超时，自动取消");
            }
        }

    }

    /**
     * 自动处理派送中订单
     */
    @Scheduled(cron = "0 20 * * * ? ")
    public void processDeliveryOrder() {
        log.info("定时处理派送中订单");

        List<Orders> ordersList = orderMapper.getByStatusOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));

        if(!ordersList.isEmpty()) {
            for (Orders order : ordersList) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }

    }
}
