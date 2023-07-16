package com.sky.service.impl;

import com.sky.service.SetmealService;
import com.sky.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl implements ShopService {

    public static final String KEY = "SHOP_STATUS";
    //设置key为常量

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void startOrStop(Integer status) {
        redisTemplate.opsForValue().set(KEY,status);
    }

    @Override
    public Integer getStatus() {
        Integer shop_status = (Integer)redisTemplate.opsForValue().get(KEY);

        return shop_status;
    }
}
