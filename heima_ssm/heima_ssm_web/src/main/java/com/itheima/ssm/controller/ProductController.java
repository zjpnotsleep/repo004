package com.itheima.ssm.controller;

import com.itheima.ssm.domain.Product;
import com.itheima.ssm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @RequestMapping("findAll.do")
    public ModelAndView findAll() throws Exception{
        ModelAndView mv = new ModelAndView();
        List<Product> list = productService.findAll();

        mv.addObject("productList",list);
        mv.setViewName("product-list");
        return mv;
    }

    @RequestMapping("save.do")
    public String save(Product product){
        //ModelAndView mv = new ModelAndView();

        productService.save(product);
        //mv.addObject("productList",list);
        //mv.setViewName("redirect:findAll.do");
        return "redirect:findAll.do";
        //return mv;
    }
    /*@RequestMapping("save.do")
    public ModelAndView save(Product product){
        ModelAndView mv = new ModelAndView();

        productService.save(product);
        //mv.addObject("productList",list);
        mv.setViewName("redirect:findAll.do");
        //return "redirect:findAll.do";
        return mv;
    }*/
}



