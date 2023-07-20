package com.sky.service.impl;

import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    //统计指定时间内的营业额数据
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            //将日期一天一天加入集合
            begin = begin.plusDays(1);
            dateList.add(begin);
        }




        List<Double> turnOverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //查询date日期对应的营业额，已完成订单的金额合计
            Integer sum = 0;

            //获取一天起始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询数据库的到当日订单数据
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);


            Double turnover = orderMapper.sumByMap(map);
            //若无数据，则转为0
            if(turnover == null){
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
}
