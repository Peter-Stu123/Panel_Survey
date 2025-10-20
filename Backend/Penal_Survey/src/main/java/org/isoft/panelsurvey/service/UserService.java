package org.isoft.panelsurvey.service;

import org.isoft.panelsurvey.dto.UserLoginDTO;
import org.isoft.panelsurvey.dto.UserRegisterDTO;
import org.isoft.panelsurvey.entity.User;
import org.isoft.panelsurvey.vo.LoginVO;
import org.isoft.panelsurvey.vo.UserVO;

import java.util.List;

public interface UserService {
    
    LoginVO login(UserLoginDTO loginDTO);
    
    void register(UserRegisterDTO registerDTO);
    
    UserVO getUserById(Long id);
    
    UserVO getUserInfo(Long userId);
    
    List<UserVO> getAllUsers();
    
    void updateUser(Long id, User user);
    
    void deleteUser(Long id);
    
    void updateStatus(Long id, Integer status);
}

