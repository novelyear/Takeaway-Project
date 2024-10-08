package com.my.service;

import com.my.dto.*;
import com.my.result.PageResult;
import com.my.vo.OrderStatisticsVO;
import com.my.vo.OrderSubmitVO;
import com.my.vo.OrderVO;

public interface OrderService {

    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     */
    void payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     */
    void paySuccess(String outTradeNo);

    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO getDetail(Long id);

    void userCancelById(Long id);

    void repetition(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO getStatistics();

    void confirmOrder(OrdersConfirmDTO id);

    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    void cancel(OrdersCancelDTO ordersCancelDTO);

    void deliveryOrder(Long id);

    void completeOrder(Long id);

    void reminder(Long orderId);
}
