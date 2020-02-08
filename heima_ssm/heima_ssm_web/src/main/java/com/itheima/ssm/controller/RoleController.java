package com.itheima.ssm.controller;

import com.itheima.ssm.domain.Permission;
import com.itheima.ssm.domain.Role;
import com.itheima.ssm.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("role")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @RequestMapping("findAll.do")
    public ModelAndView findAll(){
        ModelAndView mv = new ModelAndView();
        List<Role> list = roleService.findAll();
        mv.addObject("roleList",list);
        mv.setViewName("role-list");
        return mv;
    }

    @RequestMapping("save.do")
    public String saveRole(Role role){
        roleService.save(role);
        return "redirect:findAll.do";
    }


    @RequestMapping("findById.do")
    public String findById(@RequestParam(name = "id",required = true) String roleId,Model model){
        Role role = roleService.findById(roleId);
        model.addAttribute("role",role);
        return "role-show";
    }

    @RequestMapping("findRoleByIdAndAllPermission.do")
    public String findRoleByIdAndAllPermission(@RequestParam(name = "id",required = true) String roleId,Model model){
        Role role = roleService.findRoleByRoleId(roleId);
        List<Permission> permissionList = roleService.findRoleByIdAndAllPermission(roleId);
        model.addAttribute("role",role);
        model.addAttribute("permissionList",permissionList);
        return "role-permission-add";
    }

    @RequestMapping("addPermissionToRole.do")
    public String addPermissionToRole(@RequestParam(name = "roleId",required = true) String roleId,@RequestParam(name = "ids",required = true) String[] ids){
        roleService.addPermissionToRole(roleId,ids);
        return "redirect:findAll.do";
    }
}
