package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional//操作两表，开启事务
    public void saveWithSetmealDishes(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //1.向套餐表中添加数据
        //TODO 套餐默认是起售,容易出bug
        setmealMapper.insert(setmeal);

        //1.5开启主键返回，取到id
        Long setmealId = setmeal.getId();


        //2.向套餐菜品表中添加数据


        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();


        if (setmealDishes != null && setmealDishes.size() > 0) {
            //判断套餐是否有菜品

            //TODO 未販売の料理は追加できません
            for (SetmealDish setmealDish : setmealDishes) {

                //判断是否停用
                Long dishId = setmealDish.getDishId();
                Dish dish = dishMapper.getById(dishId);
                if(dish.getStatus().equals(StatusConstant.DISABLE)){
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
                setmealDish.setSetmealId(setmealId);
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }
}
