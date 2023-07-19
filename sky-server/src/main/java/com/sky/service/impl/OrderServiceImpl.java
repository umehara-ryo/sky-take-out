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
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;

    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //处理业务异常（1.地址簿为空 2.购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            //无收获地址，抛出异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartMapper.list(ShoppingCart.builder().userId(userId).build());
        if (list == null || list.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        //1.向order表里添加一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);//查询完带主键返回

        List<OrderDetail> orderDetailList = new ArrayList<>();


        //2.向订单明细表里添加N条数据
        for (ShoppingCart shoppingCart : list) {
            OrderDetail orderDetail = OrderDetail.builder().orderId(orders.getId()).build();
            //将orderId封装进类插入数据库
            BeanUtils.copyProperties(shoppingCart, orderDetail);//cart和detail里的数据极其类似，拷贝使用
            orderDetailList.add(orderDetail);

            //orderDetailMapper.insert(orderDetail);//批量插入优于循环插入
        }

        //批量插入
        orderDetailMapper.insertBatch(orderDetailList);


        //3.清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        //4.封装VO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .orderTime(LocalDateTime.now())
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount()).build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
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
     *
     * @param outTradeNo
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


    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        //获取userID

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());


        Page<Orders> page = orderMapper.page(ordersPageQueryDTO);
        List<OrderVO> orderVOS = new ArrayList<>();
        List<Orders> orders = page.getResult();
        //如果查到数据，根据orderId查询订单详情并封装
        if (orders != null && orders.size() > 0) {


            for (Orders order : orders) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                Long orderId = order.getId();
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                orderVO.setOrderDetailList(orderDetails);
                orderVOS.add(orderVO);
            }
        }


        return new PageResult(page.getTotal(), orderVOS);
    }

    @Override
    public OrderVO lookDetails(Long id) {
        //根据orderID查询detail表
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        //根据orderID查询order表
        OrderVO orderVO = orderMapper.getById(id);

        //根据地址id查询地址值
        Long addressBookId = orderVO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        String address = addressBook.getProvinceName()
                + addressBook.getCityName()
                + addressBook.getDistrictName()
                + addressBook.getDetail();
        //封装进VO
        orderVO.setOrderDetailList(orderDetails);
        orderVO.setAddress(address);

        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) {
        //将订单状态改为取消
        Orders orders = Orders.builder().id(id).status(Orders.CANCELLED).build();
        //todo 取消订单退款未实现
        orderMapper.update(orders);
    }

    @Override
    public void oneMoreOrder(Long id) {
        //查询od表，取出订单详情数据
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);


        //将订单详情对象转为购物车对象
        List<ShoppingCart> shoppingCartList =
                orderDetails.stream().map(x -> {//匿名方法
                    ShoppingCart shoppingCart = ShoppingCart.builder()
                            .createTime(LocalDateTime.now())
                            .userId(BaseContext.getCurrentId())
                            .build();
                    //为购物车附上时间和userId
                    BeanUtils.copyProperties(x, shoppingCart, "id");//注意排除id

                    return shoppingCart;
                }).collect(Collectors.toList());


        shoppingCartMapper.insertBatch(shoppingCartList);


    }

    @Override
    public PageResult pageAll(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());


        Page<Orders> page = orderMapper.page(ordersPageQueryDTO);
        List<OrderVO> orderVOS = new ArrayList<>();
        List<Orders> orders = page.getResult();
        //如果查到数据，根据orderId查询订单详情并封装
        if (orders != null && orders.size() > 0) {


            for (Orders order : orders) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                Long orderId = order.getId();

                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                List<String> orderDishList = orderDetails.stream().map(orderDetail -> {
                    String orderDish = orderDetail.getName() + "*" + orderDetail.getNumber() + " ";
                    return orderDish;
                }).collect(Collectors.toList());
                String orderDishes = String.join("", orderDishList);
                //将orderdetails的数据拼接为OrderDishes

                orderVO.setOrderDishes(orderDishes);
                orderVOS.add(orderVO);
            }
        }


        return new PageResult(page.getTotal(), orderVOS);
    }

    @Override
    public OrderStatisticsVO statistics() {
        //待接单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        //待派送数量
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        //派送中数量
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);



        return OrderStatisticsVO.builder()
                .confirmed(confirmed)
                .toBeConfirmed(toBeConfirmed)
                .deliveryInProgress(deliveryInProgress)
                .build();
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        OrderVO orderVO = orderMapper.getById(ordersConfirmDTO.getId());
        if(orderVO.getStatus() != Orders.TO_BE_CONFIRMED){
            //判断是否为待接单
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersConfirmDTO.getId());
        orders.setStatus(Orders.CONFIRMED);

        orderMapper.update(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        OrderVO orderVO = orderMapper.getById(ordersCancelDTO.getId());
       if(orderVO.getStatus() == Orders.COMPLETED){
           throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
       }

        //已完成订单无法取消
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersCancelDTO,orders);
        orders.setStatus(Orders.CANCELLED);
        //TODO 取消要为客户退款 未实现

        orderMapper.update(orders);
    }


    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        OrderVO orderVO = orderMapper.getById(ordersRejectionDTO.getId());
        //判断是否为待接单，不是则抛异常
        if(orderVO.getStatus() != Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersRejectionDTO,orders);
        orders.setStatus(Orders.CANCELLED);
        //TODO 取消要为客户退款 未实现

        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        OrderVO orderVO = orderMapper.getById(id);
        //判断订单是否为已接单
        if(orderVO.getStatus() != Orders.CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        OrderVO orderVO = orderMapper.getById(id);
        //判断订单是否为派送中
        if(orderVO.getStatus() != Orders.DELIVERY_IN_PROGRESS){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.COMPLETED);
        //设置为已完成

        orderMapper.update(orders);
    }


}
