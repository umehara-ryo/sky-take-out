package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
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

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        //1.启用pagehelper
        PageHelper.startPage(setmealPageQueryDTO.getPage(),
                setmealPageQueryDTO.getPageSize());

        //2.查询setmeal表并连接category表
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);


        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    @Transactional//1.事務起動
    public void deleteBatch(List<Long> ids) {

        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            //1.按id查询出套餐
            if(setmeal.getStatus().equals(StatusConstant.ENABLE)){
                //2.判断套餐是否启用
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
            //3..删除setmeal表
            setmealMapper.deleteById(id);

            //4.删除setmeal_dish表
            setmealDishMapper.deleteBySetmealId(id);

        }
    }

    @Override
    public SetmealVO getByIdWithSetmealDish(Long id) {


        //1.查询表semeal
        Setmeal setmeal = setmealMapper.getById(id);
        //2.查询表setmealDish
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        //3.データをラッパーする
        SetmealVO setmealVO = SetmealVO.builder()
                .setmealDishes(setmealDishes)
                .build();

        BeanUtils.copyProperties(setmeal,setmealVO);
        return setmealVO;
    }

    @Override
    @Transactional
    public void updateWithSetmealDish(SetmealDTO setmealDTO) {
        //0.事务开启

        //1.更新setmeal表
       Setmeal setmeal = new Setmeal();
      BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        //2.根据setmealId删除set_dish表里的数据
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());

        //3.判断是否存在未启用菜品，重新添加set_dish表的数据

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            //判断套餐是否有菜品
            //TODO 未販売の料理は追加できません
            for (SetmealDish setmealDish : setmealDishes) {

                /*//判断是否停用
                Long dishId = setmealDish.getDishId();
                Dish dish = dishMapper.getById(dishId);
                if(dish.getStatus().equals(StatusConstant.DISABLE)) {
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }*/
                setmealDish.setSetmealId(setmealDTO.getId());
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }


}
