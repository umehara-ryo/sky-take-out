package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{opeid}")
    User getByOpenId(String openid);


    void insert(User user);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    @Select("select count(id) from user where create_time <=  #{endTime}")
    Integer countByEndTime(LocalDateTime end);

    @Select("select count(id) from user where create_time between #{beginTime} and #{endTime}")
    Integer countByMap(Map map);
}
