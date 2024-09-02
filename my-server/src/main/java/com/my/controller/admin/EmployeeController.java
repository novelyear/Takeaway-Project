package com.my.controller.admin;

import com.my.constant.JwtClaimsConstant;
import com.my.context.BaseContext;
import com.my.dto.EmployeeDTO;
import com.my.dto.EmployeeLoginDTO;
import com.my.dto.EmployeePageQueryDTO;
import com.my.dto.PasswordChangeDTO;
import com.my.entity.Employee;
import com.my.properties.JwtProperties;
import com.my.result.PageResult;
import com.my.result.Result;
import com.my.service.EmployeeService;
import com.my.utils.JwtUtil;
import com.my.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "员工相关接口")
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
    @ApiOperation("员工登录")
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
    @ApiOperation("员工登出")
    public Result<String> logout() {
        return Result.success();
    }
    /**
    * 新增员工
    *
    * @param employeeDTO
    */
    @PostMapping()
    @ApiOperation("新增员工")
    public Result<String> insert(@RequestBody EmployeeDTO employeeDTO) {
        log.info("inserted employee: {}", employeeDTO);
        employeeService.insert(employeeDTO);
        return Result.success();
    }
    /**
    * 分页查询员工
    */
    @GetMapping("/page")
    @ApiOperation("分页查询员工")
    public Result<PageResult> list(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("分页查询员工{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.list(employeePageQueryDTO);
        return Result.success(pageResult);
    }
    /**
    *根据id查询员工
    * @param id
     * @return Result<Employee>
    */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工")
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
    @ApiOperation("启用、禁用员工")
    public Result<String> enable(@PathVariable Integer status, Long id) {
        employeeService.enable(status, id);
        return Result.success();
    }
    /**
    *修改员工密码
    */
    @PutMapping("/editPassword")
    @ApiOperation("修改员工密码")
    public Result editPassword(@RequestBody PasswordChangeDTO passwordChangeDTO) {
        log.info("员工{}修改密码", BaseContext.getCurrentId());
        Long id;
        if(passwordChangeDTO.getId() != null) {
            id = passwordChangeDTO.getId();
        }
        else {
            id = BaseContext.getCurrentId();
        }
        String newPassword = passwordChangeDTO.getNewPassword();
        String oldPassword = passwordChangeDTO.getOldPassword();
        employeeService.editPassword(id, newPassword, oldPassword);
        return Result.success();
    }
    /**
    *修改员工信息
    *
    */
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result updateEmployee(@RequestBody EmployeeDTO employeeDTO) {
        log.info("updateEmployee: {}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }
    /**
     * 删除员工
     */
    @DeleteMapping
    @ApiOperation("删除员工")
    public Result deleteById(@RequestBody Long id) {
        log.info("delete: {}", id);
        employeeService.deleteById(id);
        return Result.success();
    }
}
