package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    //新增菜品和口味
    @Override
    @Transactional//事务注解,因为同时操纵两个表()
    //todo 启动类要先开启事务注解
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        //copy一个实体类dish
        BeanUtils.copyProperties(dishDTO, dish);


        //1.向菜品表添加数据
        dishMapper.insert(dish);

        //1.5获取insert语句生成的主键值
        //在配置文件声明中打开
        // useGeneratedKeys="true" keyProperty="id"
        Long dishId = dish.getId();


        //2.向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            //2.5 把获取到的dishid插入flavors再操作インフラ層

            dishFlavorMapper.insertBatch(flavors);

        }


    }
}
