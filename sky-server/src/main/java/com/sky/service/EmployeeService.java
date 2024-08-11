package com.sky.service;

import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void insert(Employee employee);

    PageResult list(Integer page, Integer pageSize);

    Employee getById(Long id);

    void enable(Boolean status, String id);

    void editPassword(Long id, String newPassword, String oldPassword);

    void update(Employee employee);
}
