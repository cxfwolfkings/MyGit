package com.colin.study.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.colin.study.entity.Description;
import com.colin.study.service.DescriptionService;

@Controller
@RequestMapping("/description")
public class DescriptionControler {

    @Autowired
    private DescriptionService descriptionService;

    /**
     * 通过ModelAndView对象获取信息
     */
    @RequestMapping("/infoByMV")
    public ModelAndView infoByMV() {
        Description description = descriptionService.getLastDescription();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("description", description);
        return new ModelAndView("description", model);
    }

    /**
     * 通过HttpServletRequest对象获取信息
     */
    @RequestMapping("/infoByRequest")
    public String infoByRequest(HttpServletRequest request) {
        Description description = descriptionService.getLastDescription();
        request.setAttribute("description", description);
        return "description";
    }
}
