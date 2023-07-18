package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    @PostMapping("/add")
    @ApiOperation(value = "添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车,商品信息为{}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查询购物车")
    public Result<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("查看购物车:{}", userId);

        List<ShoppingCart> list = shoppingCartService.list();
        return Result.success(list);
    }

    @PostMapping("/sub")
    @ApiOperation("删除一个")
    public Result delete(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("删除一个{}", shoppingCartDTO);
        shoppingCartService.delete(shoppingCartDTO);

        return Result.success();
    }

    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result delete() {
        log.info("清空购物车");
        shoppingCartService.clean();

        return Result.success();
    }

}
