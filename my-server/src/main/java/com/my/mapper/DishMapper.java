package com.my.mapper;

import com.github.pagehelper.Page;
import com.my.annotation.AutoFill;
import com.my.dto.DishPageQueryDTO;
import com.my.entity.Dish;
import com.my.enumeration.OperationType;
import com.my.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 新增菜品
     * @param dish
     * @return
     */
    @AutoFill(value = OperationType.INSERT)
    Long insert(Dish dish);

    /**
     * 根据id批量删除菜品
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 修改菜品
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据id获取菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 条件查询菜品:分类+状态
     * @param dish
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId} and status=#{status}")
    List<Dish> list(Dish dish);
    /**
     * 根据条件统计菜品数量
     */
    Integer countByMap(Map map);
}
