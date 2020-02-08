package com.itheima.ssm.dao;

import com.itheima.ssm.domain.SysLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysLogDao {
    @Select("select * from syslog")
    List<SysLog> findAll();

    /**
     * private String id;
     *     private Date visitTime;
     *     private String visitTimeStr;
     *     private String username;
     *     private String ip;
     *     private String url;
     *     private Long executionTime;
     *     private String method;
     */

    @Insert("insert into syslog(visitTime,username,ip,url,executionTime,method) values(#{visitTime},#{username},#{ip},#{url},#{executionTime},#{method})")
    void save(SysLog sysLog);
}
