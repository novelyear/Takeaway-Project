package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    Page<Dish> pageQuery(DishPageQueryDTO dishPageQueryDTO);
    @Insert("insert into dish(name, category_id, price, image, description) " +
            "VALUES " +
            "(#{name}, #{categoryId}, #{price}, #{image}, #{description})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);
}
