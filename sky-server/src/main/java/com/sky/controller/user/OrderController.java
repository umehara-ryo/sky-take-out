package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户端订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("提交订单{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);

        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation(value = "订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation(value = "历史订单查询")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询历史订单{}", ordersPageQueryDTO);

        PageResult pageResult = orderService.page(ordersPageQueryDTO);
        return Result.success(pageResult);
    }


    @ApiOperation(value = "查看详情")
    @GetMapping("orderDetail/{id}")
    public Result<OrderVO> lookDetails(@PathVariable Long id) {
        log.info("查看详情{}", id);
        OrderVO orderVO = orderService.lookDetails(id);
        return Result.success(orderVO);
    }

    @ApiOperation(value = "取消订单")
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {
        log.info("取消订单{}", id);
        orderService.cancelOrder(id);
        return Result.success();
    }


    @ApiOperation(value = "再来一单")
    @PostMapping("repetition/{id}")
    public Result oneMoreOrder(@PathVariable Long id) {
        log.info("取消订单{}", id);
        orderService.oneMoreOrder(id);
        return Result.success();
    }


    @ApiOperation(value = "客户催单")
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable("id") Long id) {
        log.info("客户催单{}", id);
        orderService.reminder(id);
        return Result.success();
    }






}
