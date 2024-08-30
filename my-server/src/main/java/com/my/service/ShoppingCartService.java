package com.my.service;


import com.my.dto.ShoppingCartDTO;
import com.my.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    void add(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> list();

    void clean();

    void delete(ShoppingCartDTO shoppingCartDTO);
}
