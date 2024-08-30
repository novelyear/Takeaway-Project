package com.my.mapper;

import com.github.pagehelper.Page;
import com.my.annotation.AutoFill;
import com.my.dto.EmployeePageQueryDTO;
import com.my.entity.Employee;
import com.my.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from my_take_away.employee where username = #{username}")
    Employee getByUsername(String username);
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
    @Select("select count(*) from employee")
    Long getCount();
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);

    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);

    void deleteById(Long id);

}
