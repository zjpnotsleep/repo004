package com.itheima.ssm.controller;

import com.itheima.ssm.domain.Role;
import com.itheima.ssm.domain.UserInfo;
import com.itheima.ssm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("findAll.do")
    public ModelAndView findAll(){
        ModelAndView mv = new ModelAndView();
        List<UserInfo> list = userService.findAll();
        mv.addObject("userList",list);
        mv.setViewName("user-list");
        return mv;
    }

    @RequestMapping("save.do")
    public String saveUser(UserInfo user){
        userService.save(user);
        return "redirect:findAll.do";
    }

    @RequestMapping("findById.do")
    public String findById(@RequestParam(name = "id",required = true)String id,Model model){
        UserInfo userInfo = userService.findById(id);
        model.addAttribute("user",userInfo);
        return "user-show";
    }

    //查询用户以及用户可以添加的角色
    @RequestMapping("findUserByIdAndAllRole.do")
    public String findUserByIdAndAllRole(@RequestParam(name = "id",required = true)String userId,Model model){
        UserInfo userInfo = userService.findById(userId);
        List<Role> roleList = userService.findUserByIdAndAllRole(userId);
        model.addAttribute("user",userInfo);
        model.addAttribute("roleList",roleList);
        return "user-role-add";
    }

    @RequestMapping("addRoleToUser.do")
    public String addRoleToUser(@RequestParam(name = "userId",required = true)String userId,
                                @RequestParam(name = "ids",required = true)List<String> ids,Model model){
        userService.addRoleToUser(userId,ids);
        return "redirect:findAll.do";
    }
}
