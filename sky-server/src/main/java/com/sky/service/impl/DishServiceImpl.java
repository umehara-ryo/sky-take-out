package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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
    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(dishPageQueryDTO.getPage(),
                dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());

    }

    @Override
    @Transactional//操作多个数据表，加事务注解
    // Work with multiple data tables with transaction annotations
    public void deleteBatch(List<Long> ids) {
        //1.判断菜品是否能够删除-------是否存在status = 1
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //2.判断当前菜品是否被关联-----setmeal_id = null
        List<Long> setmealIdsByDishIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        //TODO 可以优化回显哪个id不可删除
        if (setmealIdsByDishIds != null && setmealIdsByDishIds.size() > 0) {
            //查询到被关联抛出异常
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }


        for (Long id : ids) {

            //3.删除菜品数据
            dishMapper.deleteById(id);

            //二行を空白行で区切る

            //4.删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);

        }

    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {

        //1.查询dish表
        Dish dish = dishMapper.getById(id);

        //2.查询dish_flavor表
        List<DishFlavor> flavors = dishFlavorMapper.getFlavorsByDishId(id);

        //3.封装数据并返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    //修改菜品基础信息和口味信息
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        //1.修改菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        //2.修改口味表

        //2.1根据dishId删除原有口味
        Long dishId = dishDTO.getId();
        dishFlavorMapper.deleteByDishId(dishId);

        //2.2新增新的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //此时获得的falvors对象中无dishId值

        if (flavors != null && flavors.size() > 0) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder().status(status).id(id).build();
        dishMapper.update(dish);
    }

    @Override
    public List<DishVO> getByCategoryIdWithCategory(Long categoryId) {

        //1.多表查询 dish表左连接category
        List<DishVO> list = dishMapper.getByCategoryIdWithCategory(categoryId);

        return list;

    }
}
