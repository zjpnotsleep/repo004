package com.itheima.ssm.service.impl;

import com.itheima.ssm.dao.UserDao;
import com.itheima.ssm.domain.Role;
import com.itheima.ssm.domain.UserInfo;
import com.itheima.ssm.service.UserService;
import com.itheima.ssm.utils.BCryptPasswordEncoderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    //@Autowired
    //private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo info = userDao.findUserByUsername(username);
        //User user = new User(info.getUsername(),"{noop}"+info.getPassword(),getAuthorities(info.getRoles()));
        //User user = new User(info.getUsername(), "{noop}" + info.getPassword(), info.getStatus() == 0 ? false : true, true, true, true, getAuthorities(info.getRoles()));
        User user = new User(info.getUsername(), info.getPassword(), info.getStatus() == 0 ? false : true, true, true, true, getAuthorities(info.getRoles()));
        return user;
    }

    public List<SimpleGrantedAuthority> getAuthorities(List<Role> roles){
        List<SimpleGrantedAuthority> list = new ArrayList<>();
        for (Role role : roles) {
            list.add(new SimpleGrantedAuthority("ROLE_"+role.getRoleName()));
        }
        return list;
    }

    @Override
    public List<UserInfo> findAll() {
        List<UserInfo> infoList = userDao.findAll();
        return infoList;
    }

    @Override
    public void save(UserInfo userInfo) {
        userInfo.setPassword(BCryptPasswordEncoderUtils.encodePassword(userInfo.getPassword()));
        userDao.save(userInfo);
    }

    @Override
    public UserInfo findById(String id) {
        return userDao.findById(id);
    }

    @Override
    public List<Role> findUserByIdAndAllRole(String userId) {
        return userDao.findUserNotAddRoles(userId);
    }

    @Override
    public void addRoleToUser(String userId, List<String> ids) {
        for (String id : ids) {
            userDao.addRoleToUser(userId,id);
        }
    }
}
