package org.isoft.panelsurvey.service.impl;

import org.isoft.panelsurvey.dto.UserLoginDTO;
import org.isoft.panelsurvey.dto.UserRegisterDTO;
import org.isoft.panelsurvey.entity.User;
import org.isoft.panelsurvey.mapper.UserMapper;
import org.isoft.panelsurvey.service.UserService;
import org.isoft.panelsurvey.utils.JwtUtil;
import org.isoft.panelsurvey.utils.PasswordUtil;
import org.isoft.panelsurvey.vo.LoginVO;
import org.isoft.panelsurvey.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginVO login(UserLoginDTO loginDTO) {
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        if (!PasswordUtil.verifyPassword(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        UserVO userVO = convertToVO(user);
        return new LoginVO(token, userVO);
    }

    @Override
    public void register(UserRegisterDTO registerDTO) {
        User existingUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User existingEmail = userMapper.selectByEmail(registerDTO.getEmail());
        if (existingEmail != null) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(PasswordUtil.encodePassword(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setRealName(registerDTO.getRealName());
        user.setInstitution(registerDTO.getInstitution());
        user.setPhone(registerDTO.getPhone());
        user.setRole("USER");
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        return getUserById(userId);
    }

    @Override
    public List<UserVO> getAllUsers() {
        List<User> users = userMapper.selectAll();
        return users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateUser(Long id, User user) {
        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setId(id);
        userMapper.update(user);
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        userMapper.updateStatus(id, status);
    }

    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}

