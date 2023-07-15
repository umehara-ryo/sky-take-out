package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/admin/setmeal")
@Api(value = "定食に関するインターフェース")
public class SetmealController {


    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新規定食の追加")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新規定食の追加",setmealDTO);
        setmealService.saveWithSetmealDishes(setmealDTO);
        return Result.success();

    }



}
