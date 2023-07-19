package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("//admin/order")
@Slf4j
@Api(tags = "注文相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "搜索注文")
    @GetMapping("/conditionSearch")
    public Result<PageResult> search(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("搜索注文{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageAll(ordersPageQueryDTO);

        return Result.success(pageResult);
    }


    @ApiOperation(value = "各个状态的注文数量统计")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        log.info("各个状态的注文数量统计{}");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();

        return Result.success(orderStatisticsVO);
    }


    @ApiOperation(value = "查询注文详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> lookDeatils(@PathVariable Long id) {
        log.info("各个状态的注文数量统计{}");
        OrderVO orderVO = orderService.lookDetails(id);

        return Result.success(orderVO);
    }

    @ApiOperation("接受注文")
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接受注文{}",ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);

        return Result.success();
    }

    @ApiOperation("拒绝注文")
    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒绝注文{}",ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);

        return Result.success();
    }

    @ApiOperation("取消注文")
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消注文{}",ordersCancelDTO);
        orderService.cancel(ordersCancelDTO);

        return Result.success();
    }


    @ApiOperation("派送注文")
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id){
        log.info("派送注文{}",id);
        orderService.delivery(id);

        return Result.success();
    }  
    
    
    
    @ApiOperation("完成注文")
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        log.info("完成注文{}",id);
        orderService.complete(id);

        return Result.success();
    }





}
