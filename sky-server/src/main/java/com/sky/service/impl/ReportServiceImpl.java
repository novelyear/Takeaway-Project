package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;


    /**
     * 统计指定时间的营业额
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(
                Stream.iterate(begin, date -> date.plusDays(1))
                    .limit(end.toEpochDay() - begin.toEpochDay() + 1)
                    .map(DATE_FORMATTER::format)
                    .collect(Collectors.joining(",")));
        turnoverReportVO.setTurnoverList(
                Stream.iterate(begin, date -> date.plusDays(1))
                    .limit(end.toEpochDay() - begin.toEpochDay() + 1)
                    .map(date -> {
                        BigDecimal revenue = orderMapper.getRevenueByDate(date);
                        if(revenue == null) return "0.0";
                        else return revenue.toString();
                    })
                    .collect(Collectors.joining(","))
        );
        return turnoverReportVO;
    }
    /**
     * 查询用户情况
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(
                Stream.iterate(begin, date -> date.plusDays(1))
                    .limit(end.toEpochDay() - begin.toEpochDay() + 1)
                    .map(DATE_FORMATTER::format)
                    .collect(Collectors.joining(","))
        );
        String newUser = Stream.iterate(begin, date -> date.plusDays(1))
                                .limit(end.toEpochDay() - begin.toEpochDay() + 1)
                                .map(date -> {
                                    Integer newUsers = userMapper.getNewUserByDate(date);
                                    return newUsers == null ? "0" : newUsers.toString();
                                })
                                .collect(Collectors.joining(","));
        userReportVO.setNewUserList(newUser);
        userReportVO.setTotalUserList(prefixSum(newUser));
        return userReportVO;
    }

    /**
     * 查询时段订单数量
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        OrderReportVO orderReportVO = new OrderReportVO();
        orderReportVO.setDateList(
            Stream.iterate(begin, date -> date.plusDays(1))
                    .limit(end.toEpochDay() - begin.toEpochDay() + 1)
                    .map(DATE_FORMATTER::format)
                    .collect(Collectors.joining(","))
        );
        end = end.plusDays(1);
        //存放每天的订单总数
        List<Integer> orderCountList = new ArrayList<>();
        //存放每天的有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();
        for(;begin.isBefore(end);begin = begin.plusDays(1)) {
            List<Orders> orders = orderMapper.getAllOrders(begin);
            orderCountList.add(orders.size());
            Integer validOrders = 0;
            for(Orders order : orders) {
                if(order.getStatus().equals(Orders.COMPLETED)) validOrders++;
            }
            validOrderCountList.add(validOrders);
        }
        //计算时间区间内的订单总数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        //计算时间区间内的有效订单数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            //计算订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        orderReportVO.setOrderCountList(StringUtils.join(orderCountList,","));
        orderReportVO.setValidOrderCountList(StringUtils.join(validOrderCountList, ","));
        orderReportVO.setOrderCompletionRate(orderCompletionRate);
        orderReportVO.setTotalOrderCount(totalOrderCount);
        orderReportVO.setValidOrderCount(validOrderCount);
        return orderReportVO;
    }

    /**
     * 查询销量前10订单
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(begin, end);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    private String prefixSum(String input) {
        // 将输入的字符串按逗号分割，转换为整数数组
        String[] numbers = input.split(",");
        int[] nums = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            nums[i] = Integer.parseInt(numbers[i]);
        }

        // 计算前缀和
        int[] prefixSum = new int[nums.length];
        prefixSum[0] = nums[0];
        for (int i = 1; i < nums.length; i++) {
            prefixSum[i] = prefixSum[i - 1] + nums[i];
        }

        // 将前缀和数组转换为字符串
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < prefixSum.length; i++) {
            result.append(prefixSum[i]);
            if (i < prefixSum.length - 1) {
                result.append(",");
            }
        }

        return result.toString();
    }
}
