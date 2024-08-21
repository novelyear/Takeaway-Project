package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理异常：没地址、没商品？
        AddressBook book = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(book == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        List<ShoppingCart> curCarts = shoppingCartMapper.list(BaseContext.getCurrentId());
        if(curCarts == null || curCarts.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //将订单插入订单表
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(book.getPhone());
        orders.setConsignee(book.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());
        orderMapper.insert(orders);
        //插入订单明细表
        List<OrderDetail> details = new ArrayList<>();
        for(ShoppingCart shoppingCart : curCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            details.add(orderDetail);
        }
        orderDetailMapper.insertBatch(details);
        //清空用户购物车
        shoppingCartMapper.clean(BaseContext.getCurrentId());
        //封装VO
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(LocalDateTime.now())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO 订单号+付款方式
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 历史订单分页查询
     * @param ordersPageQueryDTO 分页查询DTO
     * @return 分页查询VO
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        //获取用户id
        Long userId = BaseContext.getCurrentId();
        //分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(userId);
        //查询用户id下所有order，对每个order查询其订单详细信息
        Page<Orders> orders = orderMapper.query(ordersPageQueryDTO);
        List<OrderVO> orderVOList = new ArrayList<>();
        for(Orders order : orders){
            //每个order及其details，共同组成一条orderVO
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(order.getId());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setOrderDetailList(orderDetails);
            orderVOList.add(orderVO);
        }
        return new PageResult(orders.getTotal(), orderVOList);
    }

    /**
     * 查询订单详情
     * @param id 订单id
     */
    @Override
    public OrderVO getDetail(Long id) {
        OrderVO orderVO = new OrderVO();
        Orders order = orderMapper.getById(id);
        BeanUtils.copyProperties(order, orderVO);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(order.getId());
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 用户取消订单
     */
    @Override
    public void userCancelById(Long id) {
        //检查订单是否存在
        Orders order = orderMapper.getById(id);
        if(order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (order.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // TODO 退款逻辑
        if(order.getStatus() == Orders.TO_BE_CONFIRMED){
            order.setPayStatus(Orders.REFUND);
        }
        //修改订单状态
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("用户取消");
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 用户再来一单
     */
    @Override
    public void repetition(Long id) {
        //查找之前的订单
        Orders order = orderMapper.getById(id);
        if(order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //清空用户购物车
        shoppingCartMapper.clean(BaseContext.getCurrentId());
        //更新订单信息
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(Orders.UN_PAID);
        order.setId(null);
        //为用户新增一个同样的订单
        orderMapper.insert(order);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        List<Orders> orders = orderMapper.query(ordersPageQueryDTO);
        Page<OrderVO> orderVOs = new Page<>();
        for(Orders order : orders){
            List<OrderDetail> details = orderDetailMapper.getByOrderId(order.getId());
            StringBuilder orderDishs = new StringBuilder();
            for(OrderDetail orderDetail : details){
                orderDishs.append(orderDetail.getName());
                orderDishs.append(",");
            }
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setOrderDishes(orderDishs.toString());
            orderVOs.add(orderVO);
        }
        return new PageResult(orderVOs.getTotal(), orderVOs.getResult());
    }

    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(orderMapper.getConfirmed());
        orderStatisticsVO.setDeliveryInProgress(orderMapper.getDeliveryInProgress());
        orderStatisticsVO.setToBeConfirmed(orderMapper.getToBeConfirmed());
        return orderStatisticsVO;
    }

    @Override
    public void confirmOrder(Long id) {
        //修改订单状态、填入预计送达时间
        Orders order = orderMapper.getById(id);
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(Orders.CONFIRMED);
        orderMapper.update(order);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders order = orderMapper.getById(ordersRejectionDTO.getId());
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        order.setStatus(Orders.CANCELLED);
        order.setPayStatus(Orders.REFUND);
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
        // TODO 退款逻辑
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders order = orderMapper.getById(ordersCancelDTO.getId());
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setStatus(Orders.CANCELLED);
        order.setPayStatus(Orders.REFUND);
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
        // TODO 退款逻辑
    }

    @Override
    public void deliveryOrder(Long id) {
        Orders order = orderMapper.getById(id);
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        //预计送达时间 无
        orderMapper.update(order);
    }

    @Override
    public void completeOrder(Long id) {
        Orders order = orderMapper.getById(id);
        order.setStatus(Orders.COMPLETED);
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(order);
    }
}
