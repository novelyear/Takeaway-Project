package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
