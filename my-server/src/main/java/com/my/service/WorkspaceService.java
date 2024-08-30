package com.my.service;

import com.my.vo.BusinessDataVO;
import com.my.vo.DishOverViewVO;
import com.my.vo.OrderOverViewVO;
import com.my.vo.SetmealOverViewVO;

public interface WorkspaceService {

    /**
     * 根据时间段统计营业数据
     * @return
     */
    BusinessDataVO getBusinessData();

    /**
     * 查询订单管理数据
     * @return
     */
    OrderOverViewVO getOrderOverView();

    /**
     * 查询菜品总览
     * @return
     */
    DishOverViewVO getDishOverView();

    /**
     * 查询套餐总览
     * @return
     */
    SetmealOverViewVO getSetmealOverView();

}
