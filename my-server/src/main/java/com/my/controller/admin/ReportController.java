package com.my.controller.admin;

import com.my.result.Result;
import com.my.service.ReportService;
import com.my.vo.OrderReportVO;
import com.my.vo.SalesTop10ReportVO;
import com.my.vo.TurnoverReportVO;
import com.my.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("admin/report")
@Slf4j
@Api(tags = "数据统计相关接口")
public class ReportController {
    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计
     * @param begin 开始日期
     * @param end 结束日期
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnOverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额统计 {} -> {}", begin, end);
        return Result.success(reportService.getTurnoverStatistics(begin, end));
    }
    /**
     * 用户统计
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户数量统计")
    public Result<UserReportVO> userReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("查询用户数据 {} -> {}", begin, end);
        return Result.success(reportService.getUserStatistics(begin, end));
    }
    /**
     * 订单统计
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单数量统计")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("查询订单数据 {} -> {}", begin, end);
        return Result.success(reportService.getOrdersStatistics(begin, end));
    }
    /**
     * 销量排名前10
     */
    @GetMapping("/top10")
    @ApiOperation("查询销量前十")
    public Result<SalesTop10ReportVO> getSalesTop10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("查询销量前10订单 {} -> {}", begin, end);
        return Result.success(reportService.getSalesTop10(begin, end));
    }
}
