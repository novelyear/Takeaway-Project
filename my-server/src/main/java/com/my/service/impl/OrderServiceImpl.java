package com.my.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.my.constant.MessageConstant;
import com.my.context.BaseContext;
import com.my.dto.*;
import com.my.entity.*;
import com.my.exception.AddressBookBusinessException;
import com.my.exception.OrderBusinessException;
import com.my.exception.ShoppingCartBusinessException;
import com.my.mapper.*;
import com.my.result.PageResult;
import com.my.service.OrderService;
import com.my.utils.WeChatPayUtil;
import com.my.vo.OrderStatisticsVO;
import com.my.vo.OrderSubmitVO;
import com.my.vo.OrderVO;
import com.my.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private WebSocketServer webSocketServer;

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
    public void payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        //由于无 mchid，省去微信支付逻辑，仅检查订单状态
        Orders orders = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());

        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if(!Objects.equals(orders.getStatus(), Orders.PENDING_PAYMENT)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
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

        //通过websocket向客户端推送消息
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", orders.getId());
        map.put("content", "订单号" + outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
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
        if(order.getStatus().equals(Orders.TO_BE_CONFIRMED)){
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
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 将订单详情对象转换为购物车对象
        //流式处理值得学习
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 订单条件搜索
     */
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
                orderDishs.append("*");
                orderDishs.append(orderDetail.getNumber());
                orderDishs.append(",");
            }
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setOrderDishes(orderDishs.toString());
            orderVOs.add(orderVO);
        }
        return new PageResult(orderVOs.getTotal(), orderVOs.getResult());
    }

    /**
     * 查询各状态订单数量
     */
    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(orderMapper.getConfirmed());
        orderStatisticsVO.setDeliveryInProgress(orderMapper.getDeliveryInProgress());
        orderStatisticsVO.setToBeConfirmed(orderMapper.getToBeConfirmed());
        return orderStatisticsVO;
    }

    /**
     * 接单
     */
    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        //修改订单状态、填入预计送达时间
        Orders order = orderMapper.getById(ordersConfirmDTO.getId());
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(Orders.CONFIRMED);
        orderMapper.update(order);
    }

    /**
     * 拒单
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders order = orderMapper.getById(ordersRejectionDTO.getId());
        if (order == null || !order.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        order.setStatus(Orders.CANCELLED);
        if(Objects.equals(order.getPayStatus(), Orders.PAID)) order.setPayStatus(Orders.REFUND);
        // TODO 退款逻辑
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 商家取消订单
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders order = orderMapper.getById(ordersCancelDTO.getId());
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setStatus(Orders.CANCELLED);
        if(order.getPayStatus().equals(Orders.PAID)) order.setPayStatus(Orders.REFUND);
        // TODO 退款逻辑
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 派送订单
     */
    @Override
    public void deliveryOrder(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null || !order.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        //预计送达时间 无
        orderMapper.update(order);
    }

    @Override
    public void completeOrder(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null || !order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.COMPLETED);
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void reminder(Long orderId) {
        //通过websocket向客户端推送消息
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", orderId);
        Orders order = orderMapper.getById(orderId);
        map.put("content", "订单号" + order.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }
}
