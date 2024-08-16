package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DishService {

    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void insert(DishDTO dishDTO);

    void delete(List<Long> ids);

    void update(DishDTO dishDTO);
}
