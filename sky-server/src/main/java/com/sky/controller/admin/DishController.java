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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@Api(value = "菜品管理")
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation(value = "新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        Long categoryId = dishDTO.getCategoryId();
        cleanCache("dish_" + categoryId );

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

        cleanCache("dish_*");

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

        cleanCache("dish_*");

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("设置菜品起售或停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("设置菜品起售或停售{} {}", status, id);
        dishService.startOrStop(status, id);

        cleanCache("dish_*");

        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> getByCategoryId(Long categoryId){
        log.info("根据分类id查询菜品 {}",categoryId);
        List<DishVO> list = dishService.getByCategoryIdWithCategory(categoryId);


        return Result.success(list);

    }

    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);

    }


}
