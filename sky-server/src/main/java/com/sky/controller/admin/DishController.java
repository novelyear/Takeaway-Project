package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("分页查询菜品")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询菜品");
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 新增菜品
     * @param
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result<String> insertDish(@RequestBody DishDTO dishDTO){
        log.info("新增菜品");
        dishService.insert(dishDTO);
        return Result.success();
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result<String> deleteDish(@RequestBody List<Long> ids) {
        log.info("删除菜品{}", ids);
        dishService.delete(ids);
        return Result.success();
    }

    public Result<String> updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品");
        dishService.update(dishDTO);
        return Result.success();
    }
}
