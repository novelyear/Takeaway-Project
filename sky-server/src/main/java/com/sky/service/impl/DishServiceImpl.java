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
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 菜品业务层
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return PageResult
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品");
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page =  dishMapper.pageQuery(dishPageQueryDTO);
        log.info("查询成功");
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    public void insert(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(StatusConstant.ENABLE);
        Long dishId = dishMapper.insert(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavorsMappers -> dishFlavorsMappers.setDishId(dishId));
            dishFlavorsMapper.insert(flavors);
        }
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        //是否正在售卖
        for(Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(Objects.equals(dish.getStatus(), StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //是否被套餐关联
        List<Long> setmealIds = setMealDishMapper.getSetMealDishIdsByDishIds(ids);
        if(setmealIds != null && !setmealIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        dishMapper.delete(ids);
        dishFlavorsMapper.delete(ids);
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //修改菜品自身信息
        dishMapper.update(dish);
        //修改菜品口味信息
        //1. 根据菜品id删除该菜品对应口味信息
        dishFlavorsMapper.delete(Collections.singletonList(dish.getId()));
        //2. 插入新的口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavorsMappers -> dishFlavorsMappers.setDishId(dish.getId()));
            dishFlavorsMapper.insert(flavors);
        }
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> dishFlavors = dishFlavorsMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        //先查询redis
        String key = "dish_" + dish.getCategoryId();
        List<DishVO> dishVOList = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if(dishVOList != null && !dishVOList.isEmpty()) {
            return dishVOList;
        }
        //redis missed，查找数据库
        List<Dish> dishList = dishMapper.list(dish);
        dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorsMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        //加入redis缓存
        redisTemplate.opsForValue().set(key, dishVOList);
        return dishVOList;
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> dishFlavors = dishFlavorsMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }
}
