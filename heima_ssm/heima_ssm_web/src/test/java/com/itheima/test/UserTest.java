package com.itheima.test;

import com.itheima.ssm.dao.UserDao;
import com.itheima.ssm.domain.User01;
import com.itheima.ssm.domain.UserInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

//@Component
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
//@ContextConfiguration(locations = "classpath:applicationContext.xml")     //也可以
public class UserTest {
    @Autowired
    private UserDao userDao;

    @Test
    public void testUser() {
        List<String> list = new ArrayList<>();
        list.add("6273375783664CA98994F392F250EC8C");
        list.add("7377B8750E634431A19DE49234C2C29D");
        list.add("111-222");
        //System.out.println(list);
        List<UserInfo> userInfos = userDao.findByIds(list);
        System.out.println(userInfos);
        System.out.println(userInfos.size());
    }

    @Test
    public void testUser01() {
        User01 user01 = new User01();
        List<String> list = new ArrayList<>();
        list.add("6273375783664CA98994F392F250EC8C");
        list.add("7377B8750E634431A19DE49234C2C29D");
        //list.add("111-222");
        user01.setUserIds(list);
        //System.out.println(list);
        List<UserInfo> userInfos = userDao.findByUser01(user01);
        System.out.println(userInfos);
        System.out.println(userInfos.size());
    }

}
