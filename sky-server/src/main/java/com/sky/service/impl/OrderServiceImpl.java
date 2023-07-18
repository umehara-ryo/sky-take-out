package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //处理业务异常（1.地址簿为空 2.购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            //无收获地址，抛出异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartMapper.list(ShoppingCart.builder().userId(userId).build());
        if(list == null || list.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        //1.向order表里添加一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
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
            BeanUtils.copyProperties(shoppingCart,orderDetail);//cart和detail里的数据极其类似，拷贝使用
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
}
