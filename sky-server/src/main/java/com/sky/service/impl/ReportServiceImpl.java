package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1.查询数据库获得数据---最近30天运营数据
        LocalDate beginDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTime.of(beginDate, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(endDate, LocalTime.MAX);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(beginTime, endTime);
        //2.写入excel文件
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("template/运营数据报表模板.xlsx");
        //通过自带的类加载器取到resource文件

        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //2.1填充总览数据

            //获取第一个表
            XSSFSheet sheet1 = excel.getSheetAt(0);
            //获取第二行第二个单元格设置值
            sheet1.getRow(1).getCell(1).setCellValue("时间： " + beginDate + " 到 " + endDate);

            //获得第四行
            XSSFRow row4 = sheet1.getRow(3);
            //营业额赋值
            row4.getCell(2).setCellValue(businessDataVO.getTurnover());
            //完成率赋值
            row4.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            //新增用户赋值
            row4.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第五行
            XSSFRow row5 = sheet1.getRow(4);
            //有效订单数
            row5.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            //平均客单价
            row5.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //2.2填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = beginDate.plusDays(i);
                LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

                BusinessDataVO businessDataByDate = workspaceService.getBusinessData(begin,end);
                XSSFRow row = sheet1.getRow(7 + i);
                //获取第i+8行
                row.getCell(1).setCellValue(String.valueOf(date));
                row.getCell(2).setCellValue(businessDataByDate.getTurnover());
                row.getCell(3).setCellValue(businessDataByDate.getValidOrderCount());
                row.getCell(4).setCellValue(businessDataByDate.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDataByDate.getUnitPrice());
                row.getCell(6).setCellValue(businessDataByDate.getNewUsers());

            }


            //3.通过输出流将文件下载到客户端
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);



            //4.关闭资源
            inputStream.close();
            outputStream.close();
            excel.close();


        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    //得到begin到end每天的集合数据
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
