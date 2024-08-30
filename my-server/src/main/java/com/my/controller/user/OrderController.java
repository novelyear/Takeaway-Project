package com.my.controller.user;


import com.my.context.BaseContext;
import com.my.dto.OrdersPageQueryDTO;
import com.my.dto.OrdersPaymentDTO;
import com.my.dto.OrdersSubmitDTO;
import com.my.result.PageResult;
import com.my.result.Result;
import com.my.service.OrderService;
import com.my.vo.OrderSubmitVO;
import com.my.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户下单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;
    /**
     * 用户下单
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户{}已下单", BaseContext.getCurrentId());
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO 订单号+付款方式
     */
    @PutMapping("/payment")//step2, 小程序下单/payment
    @ApiOperation("订单支付")
    public Result<String> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("用户{}正在支付", BaseContext.getCurrentId());
        orderService.payment(ordersPaymentDTO);
        return Result.success();
    }
    /**
     * 历史订单查询
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> historyOrdersList(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("历史订单查询{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        log.info("查询到{}", pageResult);
        return Result.success(pageResult);
    }
    /**
     * 查询订单详情
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderDetail(@PathVariable Long id){
        log.info("正在查询订单 {} 的详情", id);
        OrderVO orderVO = orderService.getDetail(id);
        return Result.success(orderVO);
    }
    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result<String> canelOrder(@PathVariable Long id){
        log.info("订单 {} 被用户 {} 取消", id, BaseContext.getCurrentId());
        orderService.userCancelById(id);
        return Result.success();
    }
    /**
     * 再来一单
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result<String> repetition(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户 {} 再来一单 {} ", userId, id);
        orderService.repetition(id);
        return Result.success();
    }
    /**
     * 催单
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result<String> reminder(@PathVariable("id") Long orderId) {
        log.info("订单 {} 被催促", orderId);
        orderService.reminder(orderId);
        return Result.success();
    }


}
