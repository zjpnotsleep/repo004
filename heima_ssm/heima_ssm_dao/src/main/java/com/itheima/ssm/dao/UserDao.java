package com.itheima.ssm.dao;

import com.itheima.ssm.domain.Role;
import com.itheima.ssm.domain.User01;
import com.itheima.ssm.domain.UserInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserDao {
    /**
     * private String id;
     * private String username;
     * private String email;
     * private String password;
     * private String phoneNum;
     * private int status;
     * private String statusStr;
     * private List<Role> roles;
     */
    @Select("select * from users where username = #{username}")
    @Results({
            @Result(id = true,property = "id",column = "id"),
            @Result(property = "username",column = "username"),
            @Result(property = "email",column = "email"),
            @Result(property = "password",column = "password"),
            @Result(property = "phoneNum",column = "phoneNum"),
            @Result(property = "status",column = "status"),
            @Result(property = "roles",column = "id",javaType = List.class,many = @Many(select = "com.itheima.ssm.dao.RoleDao.findRoleById")),
    })
    UserInfo findUserByUsername(String username);

    @Select("select * from users")
    List<UserInfo> findAll();

    @Insert("insert into users(username,email,password,phoneNum,status) values(#{username},#{email},#{password},#{phoneNum},#{status})")
    void save(UserInfo userInfo);

    @Select("select * from users where id = #{id}")
    @Results({
            @Result(id = true,property = "id",column = "id"),
            @Result(property = "username",column = "username"),
            @Result(property = "email",column = "email"),
            @Result(property = "password",column = "password"),
            @Result(property = "phoneNum",column = "phoneNum"),
            @Result(property = "status",column = "status"),
            @Result(property = "roles",column = "id",javaType = List.class,many = @Many(select = "com.itheima.ssm.dao.RoleDao.findRoleById")),
    })
    UserInfo findById(String userId);

    @Select("select * from users where id in (select userId from users_role where roleId = #{roleId})")
    List<UserInfo> findUserInfoByRoleId(String roleId);

    @Select("select * from role where id not in (select roleId from users_role where userId = #{userId})")
    List<Role> findUserNotAddRoles(String userId);

    @Insert("insert into users_role values(#{userId},#{roleId})")
    void addRoleToUser(@Param("userId") String userId,@Param("roleId") String roleId);

    @Select({"<script>",
            "select * from users where id in ",
            "<foreach item='item' index='index' collection='list' open='(' separator=',' close=')'> ",
            "   #{item} ",
            "</foreach>",
            "</script> "
    })
    //或
    /*@Select("<script>"+
            "select * from users where id in "+
            "<foreach item='item' index='index' collection='list' open='(' separator=',' close=')'> "+
            "   #{item} "+
            "</foreach>"+
            "</script> "
    )*/
    List<UserInfo> findByIds(List<String> list);

    @Select("<script>"+
            "select * from users where id in "+
            "<foreach item='item' index='index' collection='userIds' open='(' separator=',' close=')'> " +
            "   #{item} " +
            "</foreach>" +
            "</script> "
    )
    List<UserInfo> findByUser01(User01 user01);

    /*
    blog website:https://blog.csdn.net/shimilysj/article/details/84039261
    MyBatis在注解上使用动态SQL(@select使用if)
    为了简化,微服务项目中使用的mybatis没有用传统的xml的mapper层，而是写在了java代码中，那如何在@Select的注解中判断传入是空的情况呢。以下是我的代码
     // @Author: sunjian
     // @Description:  if device_id==null,carNo==null不传入
     // @Date: ${DATE} ${TIME}
    @Select({"<script>",
            "SELECT * FROM order_info",
            "WHERE 1=1",
            "<when test='deviceId!=null'>",
            "AND device_id = #{deviceId}",
            "</when>",
            "<when test='carNo!=null'>",
            "AND car_no = #{carNo}",
            "</when>",
            " AND STATUS != '已支付'",
            "AND TO_DAYS(created_date) = TO_DAYS(NOW()) ",
            "</script>"})
    List<OrderInfo> querycarno(@Param("carNo") String carNo,@Param("deviceId") String deviceId);*/
}
