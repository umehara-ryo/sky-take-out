package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/setmeal")
@Api(value = "定食に関するインターフェース")
public class SetmealController {


    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新規定食の追加")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新規定食の追加", setmealDTO);
        setmealService.saveWithSetmealDishes(setmealDTO);
        return Result.success();
    }


    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info(" 套餐分页查询", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("セット削除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("セット削除{}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐{}", id);
        SetmealVO setmealVO = setmealService.getByIdWithSetmealDish(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    @ApiOperation("セット情報の変更")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("セット情報の変更{}", setmealDTO);
        setmealService.updateWithSetmealDish(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐启用停用")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("套餐启用停用{} {}", status,id);
        setmealService.startOrStop(status,id);
        return  Result.success();
    }

}
