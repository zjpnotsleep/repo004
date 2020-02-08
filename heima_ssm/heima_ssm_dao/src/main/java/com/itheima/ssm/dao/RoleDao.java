package com.itheima.ssm.dao;

import com.itheima.ssm.domain.Permission;
import com.itheima.ssm.domain.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface RoleDao {
    /**
     * private String id;
     * private String roleName;
     * private String roleDesc;
     * private List<Permission> permissions;
     * private List<UserInfo> users;
     */
    @Select("select * from role where id in (select roleId from users_role where userId = #{userId})")
    @Results({
            @Result(id = true,property = "id",column = "id"),
            @Result(property = "roleName",column = "roleName"),
            @Result(property = "roleDesc",column = "roleDesc"),
            @Result(property = "permissions",column = "id",javaType = List.class,many = @Many(select = "com.itheima.ssm.dao.PermissionDao.findPermissionByRoleId")),
            @Result(property = "users",column = "id",javaType = List.class,many = @Many(select = "com.itheima.ssm.dao.UserDao.findUserInfoByRoleId")),
    })
    List<Role> findRoleById(String userId);

    @Select("select * from role")
    List<Role> findAll();

    @Insert("insert into role(roleName,roleDesc) values(#{roleName},#{roleDesc})")
    void save(Role role);

    @Select("select * from role where id = #{roleId}")
    @Results({
            @Result(id = true,property = "id",column = "id"),
            @Result(property = "roleName",column = "roleName"),
            @Result(property = "roleDesc",column = "roleDesc"),
            @Result(property = "permissions",column = "id",javaType = List.class,many = @Many(select = "com.itheima.ssm.dao.PermissionDao.findPermissionByRoleId")),
            @Result(property = "users",column = "id",javaType = List.class,many = @Many(select = "com.itheima.ssm.dao.UserDao.findUserInfoByRoleId")),
    })
    Role findRoleByRoleId(String roleId);

    @Select("select * from permission where id not in (select permissionId from role_permission where roleId = #{roleId})")
    List<Permission> findRoleByIdAndAllPermission(String roleId);

    @Insert("insert into role_permission values(#{permisssionId},#{roleId})")
    void addPermissionToRole(@Param("roleId") String roleId,@Param("permisssionId") String permisssionId);
}
