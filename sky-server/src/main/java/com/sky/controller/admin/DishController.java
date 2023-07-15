package com.sky.controller.admin;
//菜品管理

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    @ApiOperation("根据Id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation(value = "修改数据")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改数据{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("设置菜品起售或停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("设置菜品起售或停售{} {}", status, id);
        dishService.startOrStop(status, id);
        return Result.success();


    }


}
