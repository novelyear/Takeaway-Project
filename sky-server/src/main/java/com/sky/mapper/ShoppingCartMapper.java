package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time)" +
            "values " +
            "(#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{amount}, #{createTime})")
    void add(ShoppingCart shoppingCart);

    ShoppingCart query(ShoppingCart shoppingCart);

    void update(ShoppingCart shoppingCart);
    @Select("select * from shopping_cart where user_id = #{id}")
    List<ShoppingCart> list(Long userId);
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void clean(Long userId);

    void delete(ShoppingCart shoppingCart);
}
