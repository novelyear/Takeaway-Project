package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void insert(EmployeeDTO employeeDTO);

    PageResult list(EmployeePageQueryDTO employeePageQueryDTO);

    Employee getById(Long id);

    void enable(Integer status, Long id);

    void editPassword(Long id, String newPassword, String oldPassword);

    void update(EmployeeDTO employeeDTO);

    void deleteById(Long id);
}
