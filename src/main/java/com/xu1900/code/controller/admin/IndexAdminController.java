package com.xu1900.code.controller.admin;

import com.xu1900.code.service.ArticleService;
import com.xu1900.code.service.UserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 管理员~首页或跳转url
 */
@Controller
public class IndexAdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleService articleService;
    /**
     * 跳转管理员页面
     */
    @RequiresPermissions(value = "进入管理员主页")
    @RequestMapping("toAdminUserCenterPage")
    public String toAdminUserCenterPage() {
        return "/admin/index";
    }

    @RequiresPermissions(value = "进入管理员主页")
    @RequestMapping("/defaultIndex")
    public ModelAndView defaultIndex() {
        ModelAndView mav = new ModelAndView();
        Long count = userService.getCount(null, null, null);
        mav.addObject("userNum",count);
        mav.addObject("todayRegisterNum",userService.todayRegister());
        mav.addObject("todayLoginNum",userService.todayLogin());
        mav.addObject("todayPublishNum",articleService.todayPublish());
        mav.addObject("noAuditNum",articleService.noAudit());
        mav.setViewName("/admin/default");
        return mav;
    }
}

