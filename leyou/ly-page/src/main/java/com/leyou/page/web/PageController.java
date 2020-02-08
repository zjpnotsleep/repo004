package com.leyou.page.web;

import com.leyou.item.pojo.Spu;
import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class PageController {
    @Autowired
    private PageService pageService;

    @RequestMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id") Long spuId, Model model) {
        Map<String, Object> attributes = pageService.loadData(spuId);

        model.addAllAttributes(attributes);

        return "item";
    }
}
