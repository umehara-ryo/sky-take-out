package com.sky.controller.admin;
//菜品管理

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Info;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;

@Slf4j
@RestController
@Api(value = "菜品管理")
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @PostMapping
    @ApiOperation(value = "新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }


    @GetMapping("/page")
    @ApiOperation(value = "ディッシュをページ別でクエリ")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("ディッシュをページ別でクエリ{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }


    @DeleteMapping
    @ApiOperation(value = "ディッシュの削除")

    public Result delete(@RequestParam List<Long> ids) {  //此注解将字符串自动转化为集合（利用MVC框架）
        log.info("ディッシュを削除する{}", ids);
        dishService.deleteBatch(ids);


        return Result.success();

    }


}
