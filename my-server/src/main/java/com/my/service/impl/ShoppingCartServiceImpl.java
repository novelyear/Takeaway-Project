package com.my.service.impl;

import com.my.context.BaseContext;
import com.my.dto.ShoppingCartDTO;
import com.my.entity.Dish;
import com.my.entity.Setmeal;
import com.my.entity.ShoppingCart;
import com.my.mapper.DishMapper;
import com.my.mapper.SetmealMapper;
import com.my.mapper.ShoppingCartMapper;
import com.my.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    DishMapper dishMapper;
    @Autowired
    SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //得到当前用户id
        Long userId = BaseContext.getCurrentId();
        //绑定用户
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);
        shoppingCart.setCreateTime(LocalDateTime.now());
        //查询金额图片
        if(shoppingCart.getDishId() == null) {
            Long setmealId = shoppingCart.getSetmealId();
            Setmeal setmeal = setmealMapper.getSetmealBySetmealId(setmealId);
            shoppingCart.setAmount(setmeal.getPrice());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setName(setmeal.getName());
        }
        else {
            Long dishId = shoppingCart.getDishId();
            Dish dish = dishMapper.getById(dishId);
            shoppingCart.setAmount(dish.getPrice());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setName(dish.getName());
        }
        //判断购物车中是否已经存在该菜品或套餐
        ShoppingCart exist = shoppingCartMapper.query(shoppingCart);
        if(exist == null) {
            //如果已经添加，则number+1，如果没有，则新增
            shoppingCart.setNumber(1);
            shoppingCartMapper.add(shoppingCart);
        }
        else {
            Integer number = exist.getNumber() + 1;
            exist.setNumber(number);
            shoppingCartMapper.update(exist);
        }
    }

    @Override
    public List<ShoppingCart> list() {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        //查询当前用户id对应的所有套餐和菜品
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(userId);
        //返回购物车

        return shoppingCarts;
    }

    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.clean(userId);
    }

    @Override
    public void delete(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ShoppingCart exist = shoppingCartMapper.query(shoppingCart);
        if(exist != null) {
            Integer number = exist.getNumber();
            if(number == 1) {
                shoppingCartMapper.delete(shoppingCart);
            }
            else {
                exist.setNumber(number - 1);
                shoppingCartMapper.update(exist);
            }
        }
    }
}
