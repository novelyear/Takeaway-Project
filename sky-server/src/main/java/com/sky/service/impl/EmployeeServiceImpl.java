package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    /**
     * 启用、禁用员工
     * @param status id
     *
     */
    @Override
    public void enable(Boolean status, String id) {
        employeeMapper.enable(status, Integer.valueOf(id));
    }

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     *
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
//        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        //3、返回实体对象
        return employee;
    }

    @Override
    public void insert(Employee employee) {
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        if(employee.getPassword() == null) employee.setPassword(PasswordConstant.DEFAULT_PASSWORD);
        employeeMapper.insert(employee);
    }

    @Override
    public PageResult list(Integer page, Integer pageSize) {
        PageResult pageResult = new PageResult();
        pageResult.setTotal(employeeMapper.count());
        Integer start = (page - 1) * pageSize;
        pageResult.setRecords(employeeMapper.list(start, pageSize));
        return pageResult;
    }

    @Override
    public Employee getById(Long id) {
        return employeeMapper.getById(id);
    }

    @Override
    public void editPassword(Long id, String newPassword, String oldPassword) {
        //1、根据id查询数据库中的数据
        Employee employee = employeeMapper.getById(id);
        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
//        oldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!oldPassword.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        employeeMapper.editPassword(id, newPassword);
    }

    @Override
    public void update(Employee employee) {
        Employee toUpdate = employeeMapper.getById(employee.getId());
        if (toUpdate == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (toUpdate.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        employeeMapper.update(employee);
    }
}
