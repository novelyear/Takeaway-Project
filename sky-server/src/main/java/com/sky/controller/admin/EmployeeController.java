package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }
    /**
    * 新增员工
    *
    * @param employeeDTO
    */
    @PostMapping()
    public Result<String> insert(@RequestBody EmployeeDTO employeeDTO) {
        log.info("inserted employee: {}", employeeDTO);

        employeeService.insert(employeeDTO);
        return Result.success();
    }
    /**
    * 分页查询员工
    *
    *
    */
    @PostMapping("/page")
    public Result<PageResult> list(EmployeePageQueryDTO employeePageQueryDTO) {
        PageResult pageResult = employeeService.list(employeePageQueryDTO);
        return Result.success(pageResult);
    }
    /**
    *根据id查询员工
    * @param id
     * @return Result<Employee>
    */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("getById: {}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 启用、禁用员工
     * @param id
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> enable(@PathVariable Boolean status, @RequestParam String id) {
        employeeService.enable(status, id);
        return Result.success();
    }
    /**
    *修改员工密码
     * @param id
     * @param newPassword
     * @param oldPassword
    */
    @PutMapping("/editPassword")
    public Result editPassword(@RequestBody Long id,
                               @RequestParam String newPassword,
                               @RequestParam String oldPassword) {
        log.info("employee{}'s password updated", id);
        employeeService.editPassword(id, newPassword, oldPassword);
        return Result.success();
    }
    /**
    *修改员工信息
    *
    */
    @PutMapping
    public Result updateEmployee(@RequestBody Employee employee) {
        log.info("updateEmployee: {}", employee);
        employeeService.update(employee);
        return Result.success();
    }
}
