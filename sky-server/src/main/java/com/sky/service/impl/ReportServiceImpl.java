package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    //统计指定时间内的营业额数据
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = getDateList(begin, end);


        List<Double> turnOverList = new ArrayList<>();
        //创建存放营业额的集合

        for (LocalDate date : dateList) {
            //查询date日期对应的营业额，已完成订单的金额合计


            //获取一天起始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询数据库的到当日订单数据
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);


            Double turnover = orderMapper.sumByMap(map);
            //若无数据，则转为0
            if (turnover == null) {
                turnover = 0.0;
            }


            turnOverList.add(turnover);
            //将营业额放入集合

        }


        //将集合拼接成字符串
        return new TurnoverReportVO(
                StringUtils.join(dateList, ","),
                StringUtils.join(turnOverList, ","));
    }


    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        //获取日期的集合

        //查询用户总量


        //查询当日注册用户量（creatTime在当日）
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();


        for (LocalDate date : dateList) {
            //查询date日期对应的营业额

            //获取一天起始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询数据库的到当日用户数据
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);

            Integer newUser = userMapper.countByMap(map);
            Integer totalUser = userMapper.countByEndTime(endTime);

            //若无数据，则转为0
            if (newUser == null) {
                newUser = 0;
            }
            if (totalUser == null) {
                totalUser = 0;
            }


            newUserList.add(newUser);
            totalUserList.add(totalUser);
            //将员工数据放入集合
        }


        return new UserReportVO(StringUtils.join(dateList, ","),
                StringUtils.join(totalUserList, ","),
                StringUtils.join(newUserList, ",")
        );

    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);


        //查询当日订单数（orderTime在当日）
        List<Integer> orderCountList = new ArrayList<>();
        //查询当日有效订单数（orderTime在当日）
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //查询date日期对应的营业额

            //获取一天起始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询数据库的到当日订单数据
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);

            Integer orderCount = orderMapper.countByMap(map);
            //查询当日总订单

            map.put("status", Orders.COMPLETED);
            Integer validOrder = orderMapper.countByMap(map);
            //查询当日有效订单

            //若无数据，则转为0
            if (orderCount == null) {
                orderCount = 0;
            }
            if (validOrder == null) {
                validOrder = 0;
            }

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrder);
            //将员工数据放入集合
        }
        //查询当日订单完毕

        //查询总订单和总有效订单和订单完成率
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        Double orderCompletionRate = validOrderCount / totalOrderCount * 1.0;

        return OrderReportVO.builder()
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ",")).build();

    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {


        //查询date日期对应的营业额

        //获取一天起始时间和结束时间
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> nameList = salesTop10.stream().map(x -> {
            return x.getName();
        }).collect(Collectors.toList());
        List<Integer> numberList = salesTop10.stream().map(x -> {
            return x.getNumber();
        }).collect(Collectors.toList());

        return new SalesTop10ReportVO(
                StringUtils.join(nameList, ","),
                StringUtils.join(numberList, ","));


    }


    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            //将日期一天一天加入集合
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

}
