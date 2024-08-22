package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "管理端订单相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 条件查询订单
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("条件查询订单")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("管理端条件查询");
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各状态订单数量统计
     */
    @GetMapping("/statistics")
    @ApiOperation("状态订单数量统计")
    public Result<OrderStatisticsVO> getOrderStatistics() {
        log.info("状态订单数量统计");
        OrderStatisticsVO orderStatisticsVO = orderService.getStatistics();
        return Result.success(orderStatisticsVO);
    }
    /**
     * 根据id查询订单详情
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getOrderDetails(@PathVariable Long id) {
        return Result.success(orderService.getDetail(id));
    }
    /**
     * 接单
     */
    @PutMapping("/confirm")
    public Result<String> confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("{} 号订单已被接单", ordersConfirmDTO.getId());
        orderService.confirmOrder(ordersConfirmDTO);
        return Result.success();
    }
    /**
     * 拒单
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result<String> rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("{} 号订单已拒绝，原因为 {}", ordersRejectionDTO.getId(), ordersRejectionDTO.getRejectionReason());
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    @ApiOperation("管理端取消订单")
    public Result<String> cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("{} 号订单已取消，原因为 {}", ordersCancelDTO.getId(), ordersCancelDTO.getCancelReason());
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }
    /**
     * 管理端设置订单派送
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("管理端设置订单已派送")
    public Result<String> deliveryOrder(@PathVariable Long id) {
        log.info("{} 号订单派送中~", id);
        orderService.deliveryOrder(id);
        return Result.success();
    }
    /**
     * 完成订单
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result<String> completeOrder(@PathVariable Long id) {
        log.info("{} 号订单已送达~", id);
        orderService.completeOrder(id);
        return Result.success();
    }
}
