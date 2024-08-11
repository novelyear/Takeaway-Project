package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
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
    @Insert("insert into my_take_away.employee (id_number, name, password, phone, sex, username, create_time, update_time) values (" +
            "#{idNumber}, #{name}, #{password}, #{phone}, #{sex}, #{username}, #{createTime}, #{updateTime})")
    void insert(Employee employee);
    @Select("select count(*) from my_take_away.employee;")
    Integer count();

    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void update(Employee employee);
}
