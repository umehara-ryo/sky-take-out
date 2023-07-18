package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {


    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //1.判断商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long useId = BaseContext.getCurrentId();//通过ThreadLocal携带userId
        shoppingCart.setUserId(useId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //这里用list是方便以后使用


        //2.存在数量加1,并更新数据
        if(list != null && list.size() > 0){
            ShoppingCart cart = list.get(0);
            //取出唯一的数据
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
            //根据id把数量改为+1
        }else { //3.不存在插入一条购入车数据

            //判断添加到购物车的是菜品还是套餐
            Long dishId = shoppingCart.getDishId();
            Long setmealId = shoppingCart.getSetmealId();

            if(dishId != null){//本次添加为菜品

                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());




            }
            else {//本次添加为套餐

                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }

            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
            //插入购物车表
        }


    }

    @Override
    public List<ShoppingCart> list() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        return list;
    }

    @Override
    public void delete(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long useId = BaseContext.getCurrentId();//通过ThreadLocal携带userId
        shoppingCart.setUserId(useId);


        //1.查询到number为1删除,大于1则减一
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //2.存在数量减1,并更新数据
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            //取出唯一的数据
            Integer currentNumber = cart.getNumber();
            if(currentNumber == 1)
            {
                shoppingCartMapper.deleteOne(shoppingCart);
            }else {

                cart.setNumber(currentNumber - 1);
                shoppingCartMapper.updateNumberById(cart);
                //购物车里id不重要,所以不需要取出id直接用cart

            }
        }
    }

    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }
}
