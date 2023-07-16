package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "ショップにまつわるインターフェース")
public class ShopController {


    @Autowired
    private ShopService shopService;

    @ApiOperation(value = "设置营业状态")
    @PutMapping("/{status}")
    public Result startOrStop(@PathVariable Integer status) {
        log.info("设置营业状态{}", status == 1 ? "営業中" : "　閉店");
        shopService.startOrStop(status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation(value = "获取店铺营业状态")
    public Result<Integer> getStatus() {
        Integer status = shopService.getStatus();
        log.info("获取店铺营业状态为{}",status == 1 ? "営業中" : "　閉店");

        return Result.success(status);
    }


}
