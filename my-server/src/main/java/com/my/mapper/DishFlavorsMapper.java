package com.my.mapper;

import com.my.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorsMapper {

    void insert(List<DishFlavor> flavors);

    void delete(List<Long> ids);

    @Select("select * from dish_flavor where dish_flavor.dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);

    void update(List<DishFlavor> flavors);
}
