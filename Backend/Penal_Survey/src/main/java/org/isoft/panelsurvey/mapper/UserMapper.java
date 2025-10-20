package org.isoft.panelsurvey.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.isoft.panelsurvey.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {
    
    User selectById(@Param("id") Long id);
    
    User selectByUsername(@Param("username") String username);
    
    User selectByEmail(@Param("email") String email);
    
    List<User> selectAll();
    
    int insert(User user);
    
    int update(User user);
    
    int deleteById(@Param("id") Long id);
    
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}

