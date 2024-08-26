package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 新增订单
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     */
    void update(Orders orders);

    /**
     * 分页查询订单
     */
    Page<Orders> query(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 查询各个状态下订单数量
     */
    @Select("select count(*) from orders where status = 3")
    Integer getConfirmed();
    @Select("select count(*) from orders where status = 4")
    Integer getDeliveryInProgress();
    @Select("select count(*) from orders where status = 2")
    Integer getToBeConfirmed();

    /**
     * 处理超时订单
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusOrderTimeLT(Integer status, LocalDateTime orderTime);
    @Select("select sum(orders.amount) from orders where date(order_time) = #{date} and status = 5")
    BigDecimal getRevenueByDate(LocalDate date);
    @Select("select count(*) from user where date (create_time) = #{date};")
    Integer getNewUserByDate(LocalDate date);

    /**
     * 查询单日所有订单
     */
    @Select("select * from orders where date(order_time) = #{date};")
    List<Orders> getAllOrders(LocalDate date);

    List<GoodsSalesDTO> getSalesTop10(LocalDate begin, LocalDate end);
}
