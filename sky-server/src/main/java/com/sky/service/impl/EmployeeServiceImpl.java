package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Slf4j
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
    public void enable(Integer status, Long id) {
        Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);
        employeeMapper.update(employee);
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
        password = DigestUtils.md5DigestAsHex(password.getBytes());
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
    public void insert(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        if(employee.getPassword() == null) employee.setPassword(PasswordConstant.DEFAULT_PASSWORD);
        log.info("Entity HashCode after modification: {}", System.identityHashCode(employee));
        employeeMapper.insert(employee);
    }

    @Override
    public PageResult list(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("*****");
        return employee;
    }

    @Override
    public void editPassword(Long id, String newPassword, String oldPassword) {
        Employee employee = employeeMapper.getById(id);
        //密码比对
        oldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!oldPassword.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        employee.setPassword(newPassword);
        employeeMapper.update(employee);
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee toUpdate = new Employee();
        BeanUtils.copyProperties(employeeDTO, toUpdate);
        if (toUpdate.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        employeeMapper.update(toUpdate);
    }

    @Override
    public void deleteById(Long id) {
        employeeMapper.deleteById(id);
    }
}
