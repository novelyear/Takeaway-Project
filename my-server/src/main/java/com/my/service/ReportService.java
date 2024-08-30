package com.my.service;

import com.my.vo.OrderReportVO;
import com.my.vo.SalesTop10ReportVO;
import com.my.vo.TurnoverReportVO;
import com.my.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {

    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);
}
