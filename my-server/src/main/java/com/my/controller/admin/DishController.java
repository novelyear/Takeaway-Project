package com.my.controller.admin;

import com.my.dto.DishDTO;
import com.my.dto.DishPageQueryDTO;
import com.my.result.PageResult;
import com.my.result.Result;
import com.my.service.DishService;
import com.my.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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
     * @return PageResult
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
     * @param dishDTO
     * @return String
     */
    @ApiOperation("新增菜品")
    @PostMapping
    @CacheEvict(cacheNames = "dishCache", key = "#dishDTO.categoryId")
    public Result<String> insertDish(@RequestBody DishDTO dishDTO){
        log.info("新增菜品");
        dishService.insert(dishDTO);
        return Result.success();
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return string
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<String> deleteDish(@RequestBody List<Long> ids) {
        log.info("删除菜品{}", ids);
        dishService.delete(ids);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询回显")
    public Result<DishVO>  getById(@PathVariable Long id){
        DishVO dishVo = dishService.getByIdWithFlavor(id);
        return Result.success(dishVo);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return string
     */
    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames = "dishCache", key = "#dishDTO.categoryId")
    public Result<String> updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品");
        dishService.update(dishDTO);
        return Result.success();
    }
}
