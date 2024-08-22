package com.sky.controller.notify;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付回调相关接口
 */
@RestController
@RequestMapping("/notify")
@Slf4j
public class PayNotifyController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/paysuccess")
    @ApiOperation("支付成功回调函数")
    public Result<String> PaySuccess(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        log.info("订单 {} 支付成功", ordersPaymentDTO.getOrderNumber());
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success();
    }

}
