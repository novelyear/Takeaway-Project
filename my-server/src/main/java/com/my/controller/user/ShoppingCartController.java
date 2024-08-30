package com.my.controller.user;

import com.my.dto.ShoppingCartDTO;
import com.my.entity.ShoppingCart;
import com.my.result.Result;
import com.my.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "购物车相关接口")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 购物车单项增加
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车新增一项：{}", shoppingCartDTO);

        shoppingCartService.add(shoppingCartDTO);

        return Result.success();
    }
    /**
     * 查询购物车
     */
    @GetMapping("/list")
    @ApiOperation("查询购物车")
    public Result<List<ShoppingCart>> list() {
        log.info("查询购物车");

        List<ShoppingCart> shoppingCarts = shoppingCartService.list();

        return Result.success(shoppingCarts);
    }
    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean() {
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }
    /**
     * 购物车单项删除
     */
    @PostMapping("/sub")
    @ApiOperation("删除购物车单项商品")
    public Result delete(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车中的{}", shoppingCartDTO);
        shoppingCartService.delete(shoppingCartDTO);
        return Result.success();
    }
}
