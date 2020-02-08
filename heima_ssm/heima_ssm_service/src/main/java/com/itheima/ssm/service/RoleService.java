package com.itheima.ssm.service;

import com.itheima.ssm.domain.Permission;
import com.itheima.ssm.domain.Role;

import java.util.List;

public interface RoleService {
    List<Role> findAll();

    void save(Role role);

    Role findRoleByRoleId(String roleId);

    List<Permission> findRoleByIdAndAllPermission(String roleId);

    void addPermissionToRole(String roleId, String[] ids);

    Role findById(String roleId);
}
