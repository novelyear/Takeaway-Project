package com.my.service;

import com.my.dto.DishDTO;
import com.my.dto.DishPageQueryDTO;
import com.my.entity.Dish;
import com.my.result.PageResult;
import com.my.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DishService {

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void insert(DishDTO dishDTO);

    void delete(List<Long> ids);

    void update(DishDTO dishDTO);

    DishVO getByIdWithFlavor(Long id);

    List<DishVO> listWithFlavor(Dish dish);
}
