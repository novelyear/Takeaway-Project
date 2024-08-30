package com.my.service;

import com.my.dto.EmployeeDTO;
import com.my.dto.EmployeeLoginDTO;
import com.my.dto.EmployeePageQueryDTO;
import com.my.entity.Employee;
import com.my.result.PageResult;

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
