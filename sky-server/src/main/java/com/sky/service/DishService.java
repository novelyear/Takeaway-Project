package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DishService {

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void insert(DishDTO dishDTO);

    void delete(List<Long> ids);

    void update(DishDTO dishDTO);

    DishVO getByIdWithFlavor(Long id);
}
